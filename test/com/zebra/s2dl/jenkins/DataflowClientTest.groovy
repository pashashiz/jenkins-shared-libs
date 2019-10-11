package com.zebra.s2dl.jenkins

import org.junit.Test

class DataflowClientTest {

  @Test
  void drain() {
    new DataflowClient(script: null)
        .drain(name: "spg-zpc-pubsub-to-application-pipeline-.+", wait: true)
  }
}
