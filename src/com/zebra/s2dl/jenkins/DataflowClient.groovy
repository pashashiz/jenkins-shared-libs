package com.zebra.s2dl.jenkins

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.dataflow.*
import com.google.api.services.dataflow.model.Job
import com.google.api.services.dataflow.model.ListJobsResponse
import com.google.cloud.ServiceOptions

import java.time.Duration
import java.time.Instant
import java.time.Period
import java.util.concurrent.Executors;

@Grapes([
    @Grab(group='com.google.apis', module='google-api-services-dataflow', version='v1b3-rev266-1.25.0'),
    @Grab(group='com.google.cloud', module='google-cloud-core', version='1.65.0')]
)
class DataflowClient {

//  static void main(String[] args) {
//    new DataflowClient()
//        .drain("battery_analytic-gcs-archiver-pipeline-053950d4c3c4a1fb")
//  }

  def projectId = ServiceOptions.getDefaultProjectId();
  def credentials = GoogleCredential.getApplicationDefault();
  def dataflow = new Dataflow.Builder(
          GoogleNetHttpTransport.newTrustedTransport(),
          JacksonFactory.getDefaultInstance(),
          credentials)
      .setApplicationName("Jenkins Dataflow plugin")
      .build()

  Dataflow.Projects.Jobs jobs() {
    dataflow.projects().jobs()
  }

  List<Job> getJobs() {
    List<Job> all = []
    Dataflow.Projects.Jobs.List request = jobs().list(projectId)
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

  def drain(String name, boolean wait = false) {
    Job job = jobs.find { it.getName().matches(nameRegexp) }
    if (job != null) {
      jobs()
          .update(projectId, job.getId(), job.setRequestedState("JOB_STATE_DRAINED"))
          .execute()
      if (wait) {
        awaitCompleted(job.getId())
      }
    } else {
      return { null }
    }
  }

  void awaitCompleted(String jobId) {
    while (running(jobId) ) {
      sleep(1000)
    }
  }

  boolean running(String jobId) {
    !completed(jobId)
  }

  boolean completed(String jobId) {
    def job = jobs().get(projectId, jobId).execute()
    if (job != null) {
      ["JOB_STATE_STOPPED" ,
       "JOB_STATE_DONE",
       "JOB_STATE_FAILED",
       "JOB_STATE_CANCELLED",
       "JOB_STATE_DRAINED"]
          .contains(job.getRequestedState())
    } else {
      true
    }
  }
}
