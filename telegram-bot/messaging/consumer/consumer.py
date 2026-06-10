from aiogram import Bot
from .base import BaseConsumer
from .router import get_queue_handlers


class AuthConsumer(BaseConsumer):

    def __init__(self, rabbitmq_url: str, bot: Bot):
        super().__init__(rabbitmq_url)
        self._bot = bot

    async def start_consuming(self):
        await self._channel.set_qos(prefetch_count=10)

        queues_handlers = get_queue_handlers(self._bot)

        for queue_name, handler in queues_handlers.items():
            queue = await self._channel.declare_queue(queue_name, durable=True)
            await queue.consume(handler)