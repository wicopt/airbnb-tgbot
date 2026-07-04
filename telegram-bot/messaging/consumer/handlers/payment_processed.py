import asyncio
import logging
from aio_pika.abc import AbstractIncomingMessage
from aiogram import Bot
import json

from messaging.dto.events import PaymentProcessedEvent
from messaging.dto import GetMembersRequest
from messaging.pending import create_future, wait_for_result
from messaging.producer import AuthProducer

logger = logging.getLogger(__name__)


def create_payment_processed_handler(bot: Bot, producer: AuthProducer):
    async def handler(message: AbstractIncomingMessage):
        async with message.process():
            try:
                data = json.loads(message.body.decode())
            except json.JSONDecodeError:
                logger.error("Payment processed event: невалидный JSON")
                return

            event = PaymentProcessedEvent.from_dict(data)

            new_payments = [
                p for p in event.saved_payments if not p.already_exists]
            if not new_payments:
                logger.info(
                    "Payment processed event: нет новых платежей для группы %s", event.group_id)
                return

            chat_ids = await get_group_chat_ids(event.group_id, producer)
            if not chat_ids:
                logger.warning(
                    "Payment processed event: не найдено участников группы %s", event.group_id)
                return

            for chat_id in chat_ids:
                if len(new_payments) > 10:
                    text = (
                        f"💰 <b>Новые поступления</b>\n"
                        f"Обработано новых платежей: <b>{len(new_payments)}</b>"
                    )
                    try:
                        await bot.send_message(chat_id, text, parse_mode="HTML")
                    except Exception as e:
                        logger.error("Не удалось отправить сообщение %s: %s", chat_id, e)
                else:
                    for payment in new_payments:
                        text = format_payment_message(payment)
                        try:
                            await bot.send_message(chat_id, text, parse_mode="HTML")
                        except Exception as e:
                            logger.error(
                                "Не удалось отправить сообщение %s: %s", chat_id, e)

    return handler


async def get_group_chat_ids(group_id: str, producer: AuthProducer) -> list[str]:
    request = GetMembersRequest(
        requester_id="system",  # ⚠️ см. предупреждение ниже
        group_id=group_id,
    )

    create_future(request.correlation_id)
    await producer.send_get_members(request)

    try:
        response = await wait_for_result(request.correlation_id, timeout=10.0)
    except asyncio.TimeoutError:
        logger.error(
            "Auth-сервис не ответил на GetMembersRequest для группы %s", group_id)
        return []
    except KeyError:
        logger.error(
            "Ошибка связи с auth-сервисом при получении участников группы %s", group_id)
        return []

    if not response.success or not response.members:
        return []

    return [member.user_id for member in response.members]


def format_payment_message(payment) -> str:
    return (
        f"💰 <b>Новое поступление</b>\n"
        f"Квартира: <b>{payment.room_number}</b>\n"
        f"Сумма: <b>{payment.amount} USD</b>\n"
        f"Дата: {payment.payment_date}\n"
        f"Категория: {payment.category}"
    )
