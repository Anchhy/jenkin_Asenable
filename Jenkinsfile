pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '20', daysToKeepStr: '14'))
  }

  environment {
    DEPLOY_HOST = '178.128.93.188'
    DEPLOY_PATH = '/et_anchhy'
    ANSIBLE_CONFIG = 'ansible/ansible.cfg'
    ANSIBLE_HOST_KEY_CHECKING = 'False'
    TELEGRAM_API_URL = 'https://api.telegram.org'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Prepare Laravel') {
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail

if [[ ! -f .env && -f .env.example ]]; then
  cp .env.example .env
fi

if [[ -f composer.lock ]]; then
  composer install --no-interaction --prefer-dist --optimize-autoloader
else
  composer install --no-interaction --prefer-dist --optimize-autoloader
fi

php artisan key:generate --force || true

if [[ -f package-lock.json ]]; then
  npm ci
else
  npm install
fi

npm run build

php artisan config:clear
php artisan cache:clear || true
chmod -R 775 storage bootstrap/cache || true
'''
      }
    }

    stage('Deploy') {
      steps {
        sshagent(credentials: ['deploy-ssh-key']) {
          sh '''#!/usr/bin/env bash
set -euo pipefail

echo "Deploying Laravel app to ${DEPLOY_HOST}:${DEPLOY_PATH} using Ansible..."

# Run Ansible playbook
ANSIBLE_HOST_KEY_CHECKING=${ANSIBLE_HOST_KEY_CHECKING} \
ansible-playbook \
  -i ansible/inventory.ini \
  -e "deploy_path=${DEPLOY_PATH}" \
  -e "ansible_ssh_private_key_file=${HOME}/.ssh/deploy_key" \
  ansible/deploy.yml

echo "✅ Ansible deployment completed!"
'''
        }
      }
    }
  }

  post {
    success {
      echo 'Build and deployment completed successfully.'
    }

    failure {
      script {
        def subject = "[Jenkins] FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        def body = "Job: ${env.JOB_NAME}\nBuild: ${env.BUILD_NUMBER}\nURL: ${env.BUILD_URL}\nHost: ${env.DEPLOY_HOST}\nPath: ${env.DEPLOY_PATH}"

        try {
          if (env.NOTIFY_EMAIL_TO?.trim()) {
            emailext(
              to: env.NOTIFY_EMAIL_TO,
              subject: subject,
              body: body
            )
          }
        } catch (ignored) {
          echo 'Email notification skipped or failed.'
        }

        try {
          if (env.TELEGRAM_BOT_TOKEN?.trim() && env.TELEGRAM_CHAT_ID?.trim()) {
            sh '''#!/usr/bin/env bash
set -euo pipefail

message=$(cat <<EOF
Jenkins failed\nJob: ${JOB_NAME}\nBuild: ${BUILD_NUMBER}\nURL: ${BUILD_URL}
EOF
)

curl -sS -X POST "${TELEGRAM_API_URL}/bot${TELEGRAM_BOT_TOKEN}/sendMessage" \
  --data-urlencode "chat_id=${TELEGRAM_CHAT_ID}" \
  --data-urlencode "text=${message}" >/dev/null
'''
          }
        } catch (ignored) {
          echo 'Telegram notification skipped or failed.'
        }
      }
    }
  }
}
