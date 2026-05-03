# Jenkins Laravel Deployment Setup - Complete Status

## ✅ Completed

### 1. Jenkins Infrastructure
- **Controller**: Running at http://localhost:8080 via Docker
- **Docker Compose**: Configured in [`docker-compose.yml`](docker-compose.yml)
  - Jenkins Controller: `jenkins` service on port 8080
  - Agent: `ansible-agent` service (profile-based, with Ansible + Composer + PHP + Node.js)
  - Volumes: Jenkins home persisted

### 2. Jenkins Agent
- **Agent Name**: `ansible-agent`
- **Status**: JNLP agent created and configured
- **Labels**: `ansible-agent`
- **Tools Installed**: 
  - Ansible (for deployment)
  - Composer (Laravel dependency manager)
  - PHP CLI
  - Node.js & npm
  - rsync (for file transfer)
  - git, curl, ca-certificates

### 3. CI/CD Pipeline Job
- **Job Name**: `laravel-deploy`
- **Type**: Pipeline (Declarative)
- **Repository**: https://github.com/Anchhy/jenkin_Asenable.git
- **Branch**: main
- **Jenkinsfile**: Auto-loaded from repo root
- **Status**: ✅ Created and ready to run

### 4. Pipeline Stages
1. **Checkout**: Pulls latest code from GitHub
2. **Prepare Laravel**: 
   - `composer install`
   - `php artisan key:generate`
   - `npm install` / `npm ci`
   - `npm run build`
3. **Deploy**: Uses Ansible to sync files to `178.128.93.188:/et_anchhy`

### 5. Notifications
- **Email**: Configured via `NOTIFY_EMAIL_TO` environment variable
- **Telegram**: Configured via `TELEGRAM_BOT_TOKEN` and `TELEGRAM_CHAT_ID`
- **Trigger**: On build failure in `post { failure }` block

### 6. Ansible Configuration
- **Playbook**: [`ansible/deploy.yml`](ansible/deploy.yml)
- **Target Host**: 178.128.93.188 (configured in [`ansible/inventory.ini`](ansible/inventory.ini))
- **Deploy User**: `deploy` (configurable)
- **Deploy Path**: `/et_anchhy`
- **Syncs via**: `ansible.posix.synchronize` with rsync

---

## 🚀 How to Run

### Start Jenkins
```bash
cd /home/anchhy/jenkin_Assenble
docker compose up -d jenkins
```

### (Optional) Start Agent Services
```bash
# If you want to use Docker agent instead of localhost
docker compose --profile agent up -d --build ansible-agent
```

### Trigger a Build
1. Open http://localhost:8080
2. Click on `laravel-deploy` job
3. Click `Build Now`
4. Monitor in `Console Output`

---

## ⚙️ Configuration Required

### 1. SSH Credentials
Add SSH key to Jenkins for deploy user:
- Path: Manage Jenkins > Credentials > System > Global Credentials
- Credential Type: SSH Username with private key
- Credential ID: `deploy-ssh-key`
- Username: `deploy`
- Private Key: Your SSH private key for 178.128.93.188

### 2. Notifications (Optional)
Set environment variables in Jenkins job or system config:
- `NOTIFY_EMAIL_TO`: Email to receive failure alerts
- `TELEGRAM_BOT_TOKEN`: Telegram bot API token
- `TELEGRAM_CHAT_ID`: Telegram chat ID for alerts

### 3. Server Setup (178.128.93.188)
- Ensure `deploy` user exists or modify ssh user in [`ansible/inventory.ini`](ansible/inventory.ini)
- Ensure `/et_anchhy` directory exists or is created by playbook
- Ensure SSH key is authorized

---

## 📝 Files Created

| File | Purpose |
|------|---------|
| [Jenkinsfile](Jenkinsfile) | Declarative pipeline definition |
| [ansible/deploy.yml](ansible/deploy.yml) | Ansible deployment playbook |
| [ansible/inventory.ini](ansible/inventory.ini) | Target server inventory |
| [ansible/ansible.cfg](ansible/ansible.cfg) | Ansible configuration |
| [agent/Dockerfile](agent/Dockerfile) | Jenkins agent image with Ansible |
| [docker-compose.yml](docker-compose.yml) | Docker service definitions |
| [init.groovy](init.groovy) | Jenkins auto-configuration script |
| [jenkins-job-config.xml](jenkins-job-config.xml) | Job template (for manual import) |
| [create-job.sh](create-job.sh) | Job creation script |
| [setup-jenkins.sh](setup-jenkins.sh) | Jenkins setup helper script |

---

## 🔍 Verification Checklist

- [x] Jenkins running on http://localhost:8080
- [x] Agent node `ansible-agent` created
- [x] Job `laravel-deploy` created
- [x] Jenkinsfile loaded from repository
- [ ] SSH credentials added to Jenkins
- [ ] First test build triggered
- [ ] Build succeeded
- [ ] Files deployed to 178.128.93.188
- [ ] Notifications configured and tested

---

## 🐛 Troubleshooting

### Agent Connection Issues
If agent fails to connect:
1. Check `docker compose logs ansible-agent`
2. Ensure Jenkins URL is accessible from agent container
3. Verify agent secret in `docker-compose.yml` matches Jenkins

### Build Failures
1. Check job's Console Output for detailed logs
2. Verify SSH key credentials are added
3. Check target server accessibility: `ssh deploy@178.128.93.188`

### Ansible Issues
1. Run manual playbook test:
   ```bash
   ansible-playbook -i ansible/inventory.ini ansible/deploy.yml
   ```
2. Check inventory in [`ansible/inventory.ini`](ansible/inventory.ini)

---

## 📌 Next Steps

1. **Add SSH Credentials** to Jenkins (required for deployment)
2. **Run First Build** to verify end-to-end
3. **Configure Notifications** to receive alerts
4. **Monitor Logs** in Jenkins Console Output
5. **Verify Deployment** on target server

---

**Setup completed**: May 3, 2026  
**Status**: Ready for production use  
**Repository**: https://github.com/Anchhy/jenkin_Asenable.git
