import asyncio
import logging
from aiogram import Bot, Dispatcher

from config import load_config
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
    config = load_config()

    # DB
    db = Database()
    await db.connect()

    # repos
    auth_repo = AuthRepository(db)

    # bot
    bot = Bot(token=config.bot_token)
    dp = Dispatcher()

    # middleware (auth gate)
    dp.message.middleware(AuthMiddleware(auth_repo))

    # DI container
    dp["db"] = db

    # routers
    
    dp.include_router(start.router)
    dp.include_router(statistics.router)
    dp.include_router(invite.router)
    dp.include_router(members.router)
    dp.include_router(payment.router)

    # messaging
    producer = AuthProducer(config.rabbitmq_url)
    consumer = AuthConsumer(config.rabbitmq_url, bot)

    await producer.connect()
    await consumer.connect()

    # ВАЖНО: НЕ блокируем event loop
    asyncio.create_task(consumer.start_consuming())

    # DI
    dp["producer"] = producer

    logger.info("Bot started")

    try:
        await dp.start_polling(bot)
    finally:
        await producer.close()
        await consumer.close()
        await bot.session.close()


if __name__ == "__main__":
    asyncio.run(main())