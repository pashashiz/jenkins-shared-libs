package com.zebra.s2dl.jenkins

class DummyContext implements Context {

  @Override
  void log(String message) {
    println(message)
  }
}
