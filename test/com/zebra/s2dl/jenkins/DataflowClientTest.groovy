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
        .drain(name: " mdm-s3-soti-onprem-extractor-.+", wait: true)
  }
}
