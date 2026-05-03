FROM jenkins/inbound-agent:jdk17

USER root

RUN apt-get update && apt-get install -y --no-install-recommends \
    ansible \
    composer \
    curl \
    git \
    nodejs \
    npm \
    php-cli \
    php-curl \
    php-mbstring \
    php-xml \
    php-zip \
    rsync \
    unzip \
    ca-certificates \
 && rm -rf /var/lib/apt/lists/*

RUN ansible-galaxy collection install ansible.posix

USER jenkins
