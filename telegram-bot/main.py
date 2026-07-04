import asyncio
import logging
from aiogram import Bot, Dispatcher

from config import settings
from messaging.producer import AuthProducer
from messaging.consumer import AuthConsumer
from db.database import Database
from db.repositories.auth_repository import AuthRepository
from middleware.auth import AuthMiddleware

from handlers import (
    statistics, start,
    invite, members, payment
)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


async def main():
    # DB
    db = Database()
    await db.connect()

    # repos
    auth_repo = AuthRepository(db)

    # bot
    bot = Bot(token=settings.bot_token)
    dp = Dispatcher()

    # middleware
    dp.message.middleware(AuthMiddleware(auth_repo))

    # DI
    dp["db"] = db

    # routers
    dp.include_router(start.router)
    dp.include_router(statistics.router)
    dp.include_router(invite.router)
    dp.include_router(members.router)
    dp.include_router(payment.router)

    # messaging
    producer = AuthProducer(settings.rabbitmq_url)
    await producer.connect()

    consumer = AuthConsumer(settings.rabbitmq_url, bot, producer) 
    await consumer.connect()
    
    task = asyncio.create_task(consumer.start_consuming())

    dp["producer"] = producer

    logger.info("Bot started")

    try:
        await dp.start_polling(bot)
    finally:
        task.cancel()
        await producer.close()
        await consumer.close()
        await bot.session.close()


if __name__ == "__main__":
    asyncio.run(main())