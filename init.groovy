import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.jenkins.plugins.ssh.domain.ssh.auth.impl.BasicSSHUserPrivateKey

def jenkins = Jenkins.getInstance()
try {
  def store = jenkins.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
  def domain = Domain.global()
  
  def existing = store.getCredentials(domain)
  if (!existing.any { it.id == 'deploy-ssh-key' }) {
    def keyContent = '''-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAACFwAAAAdzc2gtcn
NhAAAAAwEAAQAAAgEA0csYTscAY2zZsbAZPzFQ8wtpHbTg8t4iEVfP2N3LCP+HY2LZLA6l
Q7ioUn24a+kH4IE5Nb8ubdrTYNzNoZpMrdRSbPZlnb882DoBP8n2ABJVL8My8x+t5FGY6O
Hrt9tymNYwiT/6N9u7XdjxLc1f4MbJvEMoL49kPSctaVNwR94CUC/0+JLA6ynyMeJsVyaZ
hG3wNSOccMHlAImjUlal1dmDKOdtqVha+hTGpOa1oVS/kWqT9tS9kfbpb+BfGI2N2LLob2
iOqKWYdeWFPZenoPW0wXdZ0ySlLddXfHK+e+TBMWl66mRI25VywYpSt66xwT4dG5SD3LkD
aYokwIvDx4BJhI38LpRMR4r7V1GvkpWt4NVCXrUB38v2N1kiSDnMF3aGhaDPRrVFpgYb9L
mhkVxJmitnyotO8Lx+BuqU0SL0wF1ozIqrR1GaIordcoXvsSrrTagqvky12/EDpfjZFk+5
5R40lX1Bm5aiaNZSG4F6gn1akUmuIEWDLDo6MKHHy52DCarlJp44Wo2AmH3zPoOfcdxiMw
7C2VgwRVJ3JGYzvQjdAYZz7UrIkGkXPAZHzO3Vl7FvZpzwAAFKLJdAYbf6cKUCZYKBTz
MiXJ6oZJqQdRDvCR9SV7YRyODm9Ys3SWaQbHMECUUV7YJTUyhwlqwdh5XO0AjQQTMYGT
w2g+/G1S2RKrKAiDvWqP4DvhSWJVzfAOqC8bCoQfGQVmhqp0v9n+/Q4Dj6lYnXAQxHQX
yZWwVBiGBscN8xvdQCBsCTxOV+POI1YVLlpFmAXD7cSiBl2eVnTGQzQvA3wSxNGpJXZ9
Ov0SmkpmpJHqRDPgZwfXfx4TAQ5/zqz9hM+3dj5FHYmEZXp+uGVhUAMzZUhY8WuuEFWp
pWAZKhm1F0J8peFpnVPKe+5N9XpFR9qs03UZ3O1m2PrBhPJ/CHT2T/C3B0IyXflVf2gT
pW+gshNh1C/UTqJEJm5N0SVx6HvPqxh1t6p1BAAAAA==
-----END OPENSSH PRIVATE KEY-----'''
    
    def source = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(keyContent)
    def cred = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL, 'deploy-ssh-key', 'deploy', source, '', 'Deploy SSH Key')
    store.addCredentials(domain, cred)
    jenkins.save()
    println("SSH key added")
  }
} catch (Exception e) {
  println("Credential setup: " + e.message)
}
