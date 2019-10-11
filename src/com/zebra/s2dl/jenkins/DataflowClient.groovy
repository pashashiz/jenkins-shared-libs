package com.zebra.s2dl.jenkins

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.dataflow.*
import com.google.api.services.dataflow.model.Job
import com.google.api.services.dataflow.model.ListJobsResponse
import com.google.cloud.ServiceOptions

//@Grapes([
//    @Grab(group='com.google.apis', module='google-api-services-dataflow', version='v1b3-rev266-1.25.0'),
//    @Grab(group='com.google.cloud', module='google-cloud-core', version='1.65.0')]
//)
class DataflowClient {

  static void main(String[] args) {
    System.getenv()
    new DataflowClient()
        .drain(name: "spg-zpc-pubsub-to-application-pipeline-.+", wait: true)
  }

  def projectId = ServiceOptions.getDefaultProjectId();
  def credentials = GoogleCredential.getApplicationDefault()
      .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
  def jobs = new Dataflow.Builder(
          GoogleNetHttpTransport.newTrustedTransport(),
          JacksonFactory.getDefaultInstance(),
          credentials)
      .setApplicationName("Jenkins Dataflow plugin")
      .build()
      .projects()
      .jobs()

  DataflowClient() {}

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
      print("Draining the job...")
      jobs
          .update(projectId, job.getId(), job.setRequestedState("JOB_STATE_DRAINED"))
          .execute()
      if (wait) {
        awaitCompleted(job.getId())
      }
    } else if (completing(job)) {
      if (wait) {
        awaitCompleted(job.getId())
      }
    } else {
      print("Job is already completed!")
    }
  }

  void drain(Map args) {
    drain(*args.values().toList())
  }

  void awaitCompleted(String jobId) {
    while (running(jobId) ) {
      print("Wait until job is completed...")
      sleep(1000)
    }
  }

  boolean running(String jobId) {
    !completed(jobId)
  }

  boolean completed(String jobId) {
    completed(jobs.get(projectId, jobId).execute())
  }

  static boolean completed(Job job) {
    if (job != null) {
      ["JOB_STATE_STOPPED" ,
       "JOB_STATE_DONE",
       "JOB_STATE_FAILED",
       "JOB_STATE_CANCELLED",
       "JOB_STATE_DRAINED"]
          .contains(job.getCurrentState())
    } else {
      true
    }
  }

  static boolean completing(Job job) {
    if (job != null) {
      ["JOB_STATE_DRAINING" ,
       "JOB_STATE_CANCELLING"]
          .contains(job.getCurrentState())
    } else {
      true
    }
  }

  static boolean running(Job job) {
    return !completing(job) && !completed(job)
  }
}