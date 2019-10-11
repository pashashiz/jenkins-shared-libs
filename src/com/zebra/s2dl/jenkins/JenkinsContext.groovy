package com.zebra.s2dl.jenkins

class JenkinsContext implements Context {

  def final steps

  JenkinsContext(steps) {
    this.steps = steps
  }

  @Override
  void log(String message) {
    steps.println(message)
  }
}
