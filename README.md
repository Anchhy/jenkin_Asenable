# Jenkins + Laravel + Ansible deployment

This workspace provides a Jenkins pipeline for building a Laravel project, deploying it to `178.128.93.188`, and sending failure notifications by email or Telegram.

## Files

- [Jenkinsfile](Jenkinsfile): Declarative pipeline for build, deploy, and notifications.
- [ansible/ansible.cfg](ansible/ansible.cfg): Ansible defaults.
- [ansible/inventory.ini](ansible/inventory.ini): Target server inventory.
- [ansible/deploy.yml](ansible/deploy.yml): Deployment playbook.
- [agent/Dockerfile](agent/Dockerfile): Jenkins inbound agent image with Ansible installed.

## Jenkins agent

Build the agent image and run it as a Jenkins inbound agent.

Required tools in the agent:
- Ansible
- Composer
- Node.js and npm
- PHP CLI
- rsync

The Dockerfile already installs these dependencies.

## Run Jenkins with Docker

Use the provided [docker-compose.yml](docker-compose.yml) to start a Jenkins controller locally:

- Jenkins UI: http://localhost:8080
- JNLP port: 50000

Start only the controller:

- `docker compose up -d jenkins`

Build and start the Ansible agent container after you copy the agent secret from Jenkins:

- `docker compose --profile agent up -d --build ansible-agent`

Before starting the agent, replace `CHANGE_ME` in [docker-compose.yml](docker-compose.yml) with the real Jenkins agent secret.

## Jenkins job setup

### Option 1: Import job configuration via CLI

Use the provided script to create the job automatically:

```bash
./create-job.sh http://localhost:8080 admin YOUR_API_TOKEN laravel-deploy
```

Replace:
- `admin` with your Jenkins username
- `YOUR_API_TOKEN` with your Jenkins API token (from Manage Jenkins > Security > API Token)
- `laravel-deploy` with your desired job name

### Option 2: Manual job creation in Jenkins UI

1. Open Jenkins > New Item
2. Enter a job name (e.g., `laravel-deploy`)
3. Select Pipeline
4. Click OK
5. Configure:
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: https://github.com/Anchhy/jenkin_Asenable.git
   - **Branch**: `*/main`
   - **Script Path**: `Jenkinsfile`
6. Save and run Build Now

### Jenkins credentials required

Add these to Jenkins (Manage Jenkins > System > Credentials):

1. **deploy-ssh-key**: SSH private key for deploy user on 178.128.93.188
2. **Optional**:
   - `NOTIFY_EMAIL_TO`: Email address for failure alerts
   - `TELEGRAM_BOT_TOKEN`: Telegram bot token for alerts
   - `TELEGRAM_CHAT_ID`: Telegram chat ID for alerts

## Deployment target

- Host: `178.128.93.188`
- Deploy user: `deploy`
- Path: `/et_anchhy`

### Server Setup

Run the provided setup script on the target server as root:

```bash
# Copy and run on 178.128.93.188 as root
bash /tmp/setup-deploy.sh
```

Or manually:

```bash
# Create deploy user
useradd -m -s /bin/bash deploy

# Create deployment directory
mkdir -p /et_anchhy
chown deploy:deploy /et_anchhy

# Create .ssh for SSH key authentication
mkdir -p /home/deploy/.ssh
chmod 700 /home/deploy/.ssh
chown deploy:deploy /home/deploy/.ssh

# Create authorized_keys file
touch /home/deploy/.ssh/authorized_keys
chmod 600 /home/deploy/.ssh/authorized_keys
chown deploy:deploy /home/deploy/.ssh/authorized_keys
```

Then add your SSH public key to `/home/deploy/.ssh/authorized_keys`.

### SSH Key Setup

Generate an SSH key pair on your local machine or Jenkins host:

```bash
ssh-keygen -t rsa -b 4096 -f ~/.ssh/deploy_key -N ""
cat ~/.ssh/deploy_key.pub
```

Add the public key to the server:

```bash
ssh root@178.128.93.188
echo "YOUR_PUBLIC_KEY_HERE" >> /home/deploy/.ssh/authorized_keys
exit
```

Test the connection:

```bash
ssh -i ~/.ssh/deploy_key deploy@178.128.93.188
```

### Add SSH Credentials to Jenkins

1. Go to **Manage Jenkins > Credentials > System > Global Credentials**
2. Click **Add Credentials**
3. Kind: **SSH Username with private key**
4. ID: `deploy-ssh-key`
5. Username: `deploy`
6. Add Private Key: Paste the content of `~/.ssh/deploy_key`
7. Save

## Build commands used

The pipeline runs:

- `composer install`
- `php artisan key:generate`
- `npm install`
- `npm run build`

## Notes

- The playbook uses `ansible.posix.synchronize` to copy the workspace to the server.
- The pipeline excludes `.env` from sync, so server-side environment settings remain intact.
- If the target server uses a different SSH user, update `ansible/inventory.ini`.
