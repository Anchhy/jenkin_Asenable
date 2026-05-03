import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.jenkins.plugins.ssh.domain.ssh.auth.impl.BasicSSHUserPrivateKey

try {
  def jenkins = Jenkins.getInstance()
  def store = jenkins.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
  def domain = Domain.global()
  
  def existing = store.getCredentials(domain)
  def hasKey = existing.any { it.id == 'deploy-ssh-key' }
  
  if (!hasKey) {
    def privateKey = '''-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEA1a6ij1eJ8X8X9b8X9b8X9b8X9b8X9b8X9b8X9b8X9b8X9b8X
9b8X9b8X9b8X9b8X9b8X9b8X9b8X9b8X9b8X9b8X9b8X9b8X9b8X9b8X9b8X9b8X
-----END RSA PRIVATE KEY-----'''
    
    def source = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(privateKey)
    def cred = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL, 'deploy-ssh-key', 'deploy', source, '', 'Deploy SSH Key')
    store.addCredentials(domain, cred)
    jenkins.save()
  }
} catch (Exception e) {
  println("Credentials setup: " + e.message)
}
