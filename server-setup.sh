#!/bin/bash

# Server Setup Guide for 178.128.93.188
# Run these commands as root on the target server

echo "Setting up deploy user and directories..."

# 1. Create deploy user if it doesn't exist
if ! id -u deploy > /dev/null 2>&1; then
  useradd -m -s /bin/bash deploy
  echo "Created deploy user"
fi

# 2. Create deployment directory
mkdir -p /et_anchhy
chown -R deploy:deploy /et_anchhy
chmod 755 /et_anchhy

# 3. Create .ssh directory for deploy user
mkdir -p /home/deploy/.ssh
chmod 700 /home/deploy/.ssh
chown deploy:deploy /home/deploy/.ssh

# 4. Create authorized_keys
touch /home/deploy/.ssh/authorized_keys
chmod 600 /home/deploy/.ssh/authorized_keys
chown deploy:deploy /home/deploy/.ssh/authorized_keys

# 5. Allow deploy user to create directories with sudo
cat >> /etc/sudoers.d/deploy << 'SUDOERS_EOF'
deploy ALL=(ALL) NOPASSWD: /bin/mkdir, /bin/chown, /bin/chmod, /bin/rm, /usr/bin/rsync
SUDOERS_EOF
chmod 440 /etc/sudoers.d/deploy

# 6. Create storage directories if app exists
if [ -d /et_anchhy ]; then
  mkdir -p /et_anchhy/storage/logs
  mkdir -p /et_anchhy/bootstrap/cache
  chown -R deploy:deploy /et_anchhy
fi

echo "✅ Server setup complete!"
echo ""
echo "Next steps:"
echo "1. Add your public SSH key to /home/deploy/.ssh/authorized_keys"
echo "2. Test SSH connection: ssh deploy@178.128.93.188"
echo "3. Run Jenkins build to deploy the application"
