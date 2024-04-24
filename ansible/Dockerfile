FROM amd64/debian:11

WORKDIR /app

RUN apt-get update && \
    apt-get install -y wget gnupg ansible sshpass gnupg && \
    wget -q -O- https://downloads.opennebula.org/repo/repo.key | apt-key add - && \
    echo "deb https://downloads.opennebula.org/repo/5.6/Debian/9 stable opennebula" | tee /etc/apt/sources.list.d/opennebula.list && \
    apt-get update && \
    apt-get install -y opennebula-tools
CMD ["bash", "ansible/main.sh"]