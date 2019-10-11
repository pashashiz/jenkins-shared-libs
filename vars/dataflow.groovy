import com.zebra.s2dl.jenkins.DataflowClient
import com.zebra.s2dl.jenkins.JenkinsContext

def call(Closure cl) {
  cl.resolveStrategy = Closure.DELEGATE_FIRST
  cl.delegate = new DataflowClient(new JenkinsContext(this))
  cl()
  return this
}