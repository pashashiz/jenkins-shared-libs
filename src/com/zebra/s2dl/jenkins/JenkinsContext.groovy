package com.zebra.s2dl.jenkins

class JenkinsContext implements Context {

  def steps

  JenkinsContext() {}

  void setSteps(steps) {
    this.steps = steps
  }

  @Override
  void log(String message) {
    steps.println(message)
  }
}
