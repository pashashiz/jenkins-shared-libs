package com.zebra.s2dl.jenkins

class JenkinsContext implements Context {

  private steps

  static JenkinsContext of(steps) {
    def context = new JenkinsContext()
    context.steps = steps
    return context
  }

  @Override
  void log(String message) {
    steps.println(message)
  }
}
