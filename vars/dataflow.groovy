import com.zebra.s2dl.jenkins.DataflowClient

def call(Closure) {
  def code = cl.rehydrate(new DataflowClient(), this, this)
  code.resolveStrategy = Closure.DELEGATE_ONLY
  code()
}