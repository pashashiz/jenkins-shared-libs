import com.zebra.s2dl.jenkins.DataflowClient

def call(Closure cl) {
  cl.resolveStrategy = Closure.DELEGATE_FIRST
  cl.delegate = new DataflowClient()
  cl()
  return this
}