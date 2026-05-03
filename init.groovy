import hudson.model.Computer
import hudson.slaves.DumbSlave
import hudson.slaves.JNLPLauncher
import hudson.slaves.RetentionStrategy
import jenkins.model.Jenkins
import hudson.plugins.git.GitSCM
import hudson.plugins.git.BranchSpec
import hudson.plugins.git.UserRemoteConfig
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition

def jenkins = Jenkins.getInstance()

// 1. Create agent if not exists
def agentName = 'ansible-agent'
if (jenkins.getNode(agentName) == null) {
  def launcher = new JNLPLauncher(true)
  def slave = new DumbSlave(
    agentName,
    '/home/jenkins/agent',
    launcher
  )
  slave.setNodeDescription('Ansible Agent for Laravel deployment')
  slave.setNumExecutors(1)
  slave.setMode(hudson.model.Node.Mode.NORMAL)
  slave.setRetentionStrategy(new RetentionStrategy.Always())
  slave.setLabelString('ansible-agent')
  
  jenkins.addNode(slave)
  println("Agent ${agentName} created")
}

// 2. Create Pipeline job if not exists
def jobName = 'laravel-deploy'
if (jenkins.getItem(jobName) == null) {
  def job = new WorkflowJob(jenkins, jobName)
  
  def userConfig = new UserRemoteConfig()
  userConfig.setUrl('https://github.com/Anchhy/jenkin_Asenable.git')
  userConfig.setName('origin')
  
  def definition = new CpsScmFlowDefinition(
    new GitSCM([userConfig]),
    'Jenkinsfile'
  )
  definition.getScm().getBranches().add(new BranchSpec('*/main'))
  
  job.setDefinition(definition)
  jenkins.add(job, jobName)
  jenkins.save()
  println("Job ${jobName} created successfully")
} else {
  println("Job ${jobName} already exists")
}

jenkins.save()
println("Jenkins configuration updated")
