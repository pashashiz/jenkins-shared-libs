package com.zebra.s2dl.jenkins

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.dataflow.*
import com.google.api.services.dataflow.model.Job
import com.google.api.services.dataflow.model.ListJobsResponse
import com.google.cloud.ServiceOptions
import groovy.json.JsonOutput

class DataflowClient {

  Object script

  private static def getProjectId() {
    ServiceOptions.getDefaultProjectId()
  }

  private static def getCredentials() {
    GoogleCredential.getApplicationDefault()
        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"))
  }

  private static def getJobs() {
    new Dataflow.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        JacksonFactory.getDefaultInstance(),
        credentials)
        .setApplicationName("Jenkins Dataflow plugin")
        .build()
        .projects()
        .jobs()
  }

  List<Job> list() {
    List<Job> all = []
    Dataflow.Projects.Jobs.List request = jobs.list(projectId)
    ListJobsResponse response;
    while (true) {
      response = request.execute()
      if (response.size() != 0) {
        all.addAll(response.getJobs())
        request.setPageToken(response.getNextPageToken())
      }
      if (response.getNextPageToken() == null) {
        break
      }
    }
    return all
  }

  void drain(String name, boolean wait = false) {
    Job job = list().find { it.getName().matches(name) }
    if (running(job)) {
      script.echo("Draining the job...")
      jobs
          .update(projectId, job.getId(), job.setRequestedState("JOB_STATE_DRAINED"))
          .execute()
      if (wait) {
        awaitFinished(job.getId())
      }
    } else if (finishing(job)) {
      if (wait) {
        awaitFinished(job.getId())
      }
    } else {
      script.echo("Job was already finished!")
    }
  }

  void drain(Map args) {
    drain(args.name as String, args.getOrDefault("wait", false) as Boolean)
  }

  void awaitFinished(String jobId) {
    def count = 0
    while (running(jobId) || finishing(jobId)) {
      if (count % 10 == 0) {
        script.echo("Waiting until job is finished ($count sec)...")
      }
      sleep(1000)
      count++
    }
  }

  void deploy(Map args) {
    deploy(args.jar as String, args.dataflow as String, args.options as Map)
  }

  void deploy(String jar, String dataflow, Map<String, String> options) {
    script.sh("java -cp $jar $dataflow ${asCommandLineArgs(options)}")
  }

  static String asCommandLineArgs(Map<String, Object> args) {
    args
        .collect { "--${it.key}=${escapeSpaces(serialize(it.value))}" }
        .join(" ")
  }

  static String escapeSpaces(String value) {
    value.replaceAll(" ", "\\\\ ").replaceAll("\"", "\\\\\"")
  }

  static String serialize(Object value) {
    (value instanceof Map || value instanceof Collection)
        ? JsonOutput.toJson(value)
        : value.toString()
  }

  boolean running(String jobId) {
    running(jobs.get(projectId, jobId).execute())
  }

  boolean finishing(String jobId) {
    finishing(jobs.get(projectId, jobId).execute())
  }

  boolean finished(String jobId) {
    finished(jobs.get(projectId, jobId).execute())
  }

  static boolean finished(Job job) {
    if (job != null) {
      ["JOB_STATE_STOPPED",
       "JOB_STATE_DONE",
       "JOB_STATE_FAILED",
       "JOB_STATE_CANCELLED",
       "JOB_STATE_DRAINED"]
          .contains(job.getCurrentState())
    } else {
      true
    }
  }

  static boolean finishing(Job job) {
    if (job != null) {
      ["JOB_STATE_DRAINING",
       "JOB_STATE_CANCELLING"]
          .contains(job.getCurrentState())
    } else {
      false
    }
  }

  static boolean running(Job job) {
    return !finishing(job) && !finished(job)
  }
}