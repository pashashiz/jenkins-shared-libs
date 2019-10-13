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

  @Test
  void asCommandLineArgs_whenQuotesAndSpaces() {
    def args = new DataflowClient(script: new TestScript())
        .asCommandLineArgs([
            maxNumWorkers: '5',
            labels       : '{"bu-owner-manager": "jhand"}'])
    assert args == '--maxNumWorkers=5 --labels={\\"bu-owner-manager\\":\\ \\"jhand\\"}'
  }

  @Test
  void asCommandLineArgs_whenMap() {
    def args = new DataflowClient(script: new TestScript())
        .asCommandLineArgs([
            maxNumWorkers: '5',
            labels       : [
                "bu-owner-manager": "jhand"
            ]])
    assert args == '--maxNumWorkers=5 --labels={\\"bu-owner-manager\\":\\"jhand\\"}'
  }
}
