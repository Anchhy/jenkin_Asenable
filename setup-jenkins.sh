#!/usr/bin/env bash
set -euo pipefail

JENKINS_URL="http://localhost:8080"
JENKINS_USER="admin"

# Wait for Jenkins to be ready
echo "Waiting for Jenkins to be ready..."
for i in {1..30}; do
  if curl -s "$JENKINS_URL/api/json" > /dev/null 2>&1; then
    echo "Jenkins is ready!"
    break
  fi
  echo "Attempt $i/30..."
  sleep 2
done

# Get Jenkins crumb for CSRF
CRUMB=$(curl -s "$JENKINS_URL/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)" || echo "")

# Create agent node config
AGENT_CONFIG='<?xml version="1.0" encoding="UTF-8"?>
<slave>
  <name>ansible-agent</name>
  <description>Ansible Agent for Laravel deployment</description>
  <remoteFS>/home/jenkins/agent</remoteFS>
  <numExecutors>1</numExecutors>
  <mode>NORMAL</mode>
  <retentionStrategy class="hudson.slaves.RetentionStrategy$Always"/>
  <launcher class="hudson.slaves.JNLPLauncher">
    <workDirOption class="hudson.slaves.JNLPLauncher$WorkDirOption">
      <disabled>false</disabled>
      <internalDir>remoting</internalDir>
      <failIfWorkDirIsMissing>false</failIfWorkDirIsMissing>
    </workDirOption>
    <inbound>true</inbound>
    <webSocket>false</webSocket>
    <directConnection/>
    <instanceIdentity/>
  </launcher>
  <label>ansible-agent</label>
  <nodeProperties/>
</slave>'

echo "Creating agent node..."
curl -X POST \
  -u "$JENKINS_USER" \
  -H "Content-Type: application/xml" \
  -H "$CRUMB" \
  -d "$AGENT_CONFIG" \
  "$JENKINS_URL/computer/doCreateItem?name=ansible-agent&type=hudson.slaves.DumbSlave" 2>/dev/null || echo "Note: Agent may already exist"

echo "Agent created or already exists."
echo ""
echo "========================================"
echo "Jenkins is ready at: $JENKINS_URL"
echo "========================================"
echo ""
echo "Next steps:"
echo "1. Open $JENKINS_URL in your browser"
echo "2. Go to Manage Jenkins > Security > Manage Credentials"
echo "3. Add SSH key credential (deploy-ssh-key) for deploy@178.128.93.188"
echo "4. Go to Manage Jenkins > Manage Nodes > ansible-agent"
echo "5. Copy the Agent secret"
echo "6. Update docker-compose.yml JENKINS_SECRET with that value"
echo "7. Start the agent: docker compose --profile agent up -d --build ansible-agent"
echo "8. Run the job creation:"
echo "   ./create-job.sh http://localhost:8080 admin <API_TOKEN> laravel-deploy"
echo ""
