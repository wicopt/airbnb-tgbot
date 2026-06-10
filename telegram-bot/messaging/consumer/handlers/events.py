import json
import logging
from aio_pika import IncomingMessage
from aiogram import Bot

from messaging.dto import MemberJoinedEvent

logger = logging.getLogger(__name__)


def create_member_joined_handler(bot: Bot):

    async def handler(message: IncomingMessage):
        async with message.process():
            data = json.loads(message.body)
            event = MemberJoinedEvent.from_dict(data)

            logger.info("MemberJoinedEvent: %s %s",
                        event.new_user_id, event.group_id)

            for admin_id in event.admin_ids:
                try:
                    await bot.send_message(
                        chat_id=admin_id,
                        text=(
                            f"👤 Новый участник!\n"
                            f"<code>{event.new_user_id}</code> → "
                            f"<b>{event.group_id}</b>"
                        ),
                        parse_mode="HTML",
                    )
                except Exception as e:
                    logger.error("Failed to notify %s: %s", admin_id, e)

    return handler