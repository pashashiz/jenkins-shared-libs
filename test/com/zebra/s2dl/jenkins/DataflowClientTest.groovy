package com.zebra.s2dl.jenkins

import org.junit.Test

class DataflowClientTest {

  class TestScript {
    def echo(String message) {
      println(message)
    }
  }

  @Test
  void drain() {
    new DataflowClient(script: new TestScript())
        .drain(name: "support-gcs-archiver-.+", wait: true)
  }
}
