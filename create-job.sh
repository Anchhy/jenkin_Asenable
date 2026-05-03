#!/usr/bin/env bash
set -euo pipefail

# Jenkins job import script
# Usage: ./create-job.sh <jenkins-url> <jenkins-user> <jenkins-token> <job-name>

if [[ $# -lt 4 ]]; then
  echo "Usage: $0 <jenkins-url> <jenkins-user> <jenkins-token> <job-name>"
  echo "Example: $0 http://localhost:8080 admin 123abc laravel-deploy"
  exit 1
fi

JENKINS_URL="${1%/}"
JENKINS_USER="$2"
JENKINS_TOKEN="$3"
JOB_NAME="$4"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/jenkins-job-config.xml"

if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "Error: $CONFIG_FILE not found"
  exit 1
fi

echo "Creating Jenkins job: $JOB_NAME"
echo "Jenkins URL: $JENKINS_URL"

curl -X POST \
  -u "$JENKINS_USER:$JENKINS_TOKEN" \
  -H "Content-Type: application/xml" \
  -d @"$CONFIG_FILE" \
  "$JENKINS_URL/createItem?name=$JOB_NAME"

echo ""
echo "Job created successfully!"
echo "Open: $JENKINS_URL/job/$JOB_NAME/build?delay=0sec"
