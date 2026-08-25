FROM postgres:16-alpine

RUN apk add --no-cache curl unzip bash && \
    curl https://rclone.org/install.sh | bash

COPY backup.sh /backup.sh
RUN chmod +x /backup.sh

CMD ["sleep", "infinity"]