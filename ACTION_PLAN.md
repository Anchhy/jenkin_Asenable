# 🚀 Complete Setup - Action Plan

## Why the 404 error?

1. **Job hasn't run yet** → No files deployed to the server
2. **Server not prepared** → Deploy user and directory don't exist

## ✅ What's Done

- [x] Jenkins running at http://localhost:8080
- [x] laravel-deploy job created and configured
- [x] Pipeline updated for SSH-based deployment (simpler than Ansible)
- [x] Server setup script provided

## 📋 Action Items (3 steps to deploy)

### Step 1: Set Up Deploy User & SSH Keys (5 minutes)

**Generate SSH key on your local machine or Jenkins server:**

```bash
ssh-keygen -t rsa -b 4096 -f ~/.ssh/deploy_key -N ""
```

**Set up the server (178.128.93.188):**

```bash
# SSH to server as root
ssh root@178.128.93.188

# Run these commands:
useradd -m -s /bin/bash deploy
mkdir -p /et_anchhy
chown deploy:deploy /et_anchhy
mkdir -p /home/deploy/.ssh
chmod 700 /home/deploy/.ssh
touch /home/deploy/.ssh/authorized_keys
chmod 600 /home/deploy/.ssh/authorized_keys

# Add your SSH public key
echo "YOUR_PUBLIC_KEY_CONTENT" >> /home/deploy/.ssh/authorized_keys

# Verify
exit
ssh -i ~/.ssh/deploy_key deploy@178.128.93.188
```

Or use the provided script:
```bash
# Copy server-setup.sh to your server and run as root
scp server-setup.sh root@178.128.93.188:/tmp/
ssh root@178.128.93.188 bash /tmp/server-setup.sh
```

### Step 2: Add SSH Credentials to Jenkins (2 minutes)

1. Open http://localhost:8080
2. Go to: **Manage Jenkins > Credentials > System > Global Credentials > Add Credentials**
3. Fill in:
   - **Kind**: SSH Username with private key
   - **ID**: `deploy-ssh-key`
   - **Username**: `deploy`
   - **Private Key**: Paste content of `~/.ssh/deploy_key`
4. Click **Create**

### Step 3: Trigger the Build (1 minute)

1. Open http://localhost:8080
2. Click on **laravel-deploy**
3. Click **Build Now**
4. Watch **Console Output**

If successful:
- Files sync to 178.128.93.188:/et_anchhy
- You should see your Laravel app at http://178.128.93.188/et_anchhy (if web server configured)

---

## 🔍 Verify Everything Works

```bash
# Check if files deployed
ssh deploy@178.128.93.188 ls -la /et_anchhy

# Check Laravel status
ssh deploy@178.128.93.188 "cd /et_anchhy && php artisan --version"
```

---

## 📞 Troubleshooting

### SSH key not working
- Check permissions: Private key should be 600, public key 644
- Verify authorized_keys is 600
- Test: `ssh -v -i ~/.ssh/deploy_key deploy@178.128.93.188`

### Build still fails
- Check Jenkins console output (http://localhost:8080/job/laravel-deploy)
- Verify SSH credentials ID is exactly `deploy-ssh-key`
- Ensure deploy user exists on 178.128.93.188

### Files not syncing
- Test manual rsync: `rsync -avz --delete -e "ssh -i ~/.ssh/deploy_key" . deploy@178.128.93.188:/et_anchhy/`

---

## 📝 Pipeline Summary

The build does:

1. **Checkout** - Pulls code from GitHub
2. **Prepare Laravel** - Runs:
   - `composer install`
   - `php artisan key:generate`
   - `npm install`
   - `npm run build`
3. **Deploy** - Syncs files to server using rsync over SSH

---

## 🎯 Next: Configure Notifications (Optional)

Add to Jenkins job environment variables:
- `NOTIFY_EMAIL_TO`: Email for failure alerts
- `TELEGRAM_BOT_TOKEN`: Telegram bot token
- `TELEGRAM_CHAT_ID`: Telegram chat ID

---

**You're almost there! Just set up SSH keys and run the build.**
