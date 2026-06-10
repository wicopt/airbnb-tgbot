import logging
from aio_pika import connect_robust
from aio_pika.abc import AbstractRobustConnection, AbstractChannel

logger = logging.getLogger(__name__)


class BaseConsumer:
    def __init__(self, rabbitmq_url: str):
        self._url = rabbitmq_url
        self._connection: AbstractRobustConnection | None = None
        self._channel: AbstractChannel | None = None

    async def connect(self):
        self._connection = await connect_robust(self._url)
        self._channel = await self._connection.channel()
        logger.info("Connected to RabbitMQ")

    async def close(self):
        if self._connection:
            await self._connection.close()