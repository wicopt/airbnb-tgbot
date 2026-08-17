FROM postgres:16

RUN apt-get update && apt-get install -y curl unzip && \
    curl https://rclone.org/install.sh | bash && \
    apt-get clean

COPY backup.sh /backup.sh
RUN chmod +x /backup.sh

# держим контейнер живым, чтобы ofelia могла делать exec
CMD ["sleep", "infinity"]