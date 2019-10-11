package com.zebra.s2dl.jenkins

import org.junit.Test

class DataflowClientTest {

  @Test
  void drain() {
    new DataflowClient(new DummyContext())
        .drain(name: "spg-zpc-pubsub-to-application-pipeline-.+", wait: true)
  }
}
