@Grapes([
    @Grab(group='com.google.apis', module='google-api-services-dataflow', version='v1b3-rev266-1.25.0'),
    @Grab(group='com.google.cloud', module='google-cloud-core', version='1.65.0')]
)
import com.zebra.s2dl.jenkins.*

def call(Closure cl) {
  this.echo("Dataflow")
  cl.resolveStrategy = Closure.DELEGATE_FIRST
  cl.delegate = new DataflowClient(script: this)
  cl()
  return this
}


