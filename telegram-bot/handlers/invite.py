import asyncio
import logging
from aiogram import Router
from aiogram.filters import Command
from aiogram.types import Message

from messaging.dto import UseInviteRequest, GenerateInviteRequest
from messaging.pending import create_future, wait_for_result
from messaging.producer import AuthProducer
from handlers.errors import get_error_text

router = Router()
logger = logging.getLogger(__name__)


async def safe_edit(msg: Message, text: str, **kwargs):
    try:
        return await msg.edit_text(text, **kwargs)
    except Exception as e:
        logger.warning(f"edit_text failed: {e}")
        return None


@router.message(Command("join"))
async def cmd_join(message: Message, producer: AuthProducer):
    args = message.text.split(maxsplit=1)
    if len(args) < 2:
        await message.answer("Использование: /join <код_приглашения>")
        return

    request = UseInviteRequest(
        user_id=str(message.from_user.id),
        invite_code=args[1].strip(),
    )

    create_future(request.correlation_id)

    status_msg = await message.answer("⏳ Проверяю код...")

    await producer.send_use_invite(request)

    try:
        response = await wait_for_result(request.correlation_id, timeout=10.0)
    except asyncio.TimeoutError:
        await safe_edit(status_msg, "⌛ Сервис не ответил. Попробуй позже.")
        return

    if response.success:
        await safe_edit(
            status_msg,
            f"Вы успешно вошли!",
            parse_mode="HTML",
        )
    else:
        await safe_edit(status_msg, get_error_text(response.error_code))


@router.message(Command("invite"))
async def cmd_generate_invite(
    message: Message,
    producer: AuthProducer,
    group_id: str | None
):
    if not group_id:
        await message.answer(
            "❌ Ты не в группе.\n"
            "Сначала используй /join <код>"
        )
        return

    args = message.text.split()

    request = GenerateInviteRequest(
        user_id=str(message.from_user.id),
        group_id=group_id,  
        expires_in_hours=int(args[1]) if len(args) >= 2 and args[1].isdigit() else 12,
    )

    create_future(request.correlation_id)

    status_msg = await message.answer("⏳ Генерирую код...")

    await producer.send_generate_invite(request)

    try:
        response = await wait_for_result(request.correlation_id, timeout=10.0)
    except asyncio.TimeoutError:
        await safe_edit(status_msg, "⌛ Сервис не ответил. Попробуй позже.")
        return

    if response.success:
        expires_text = (
            f"\nЧасы действия: <b>{request.expires_in_hours}</b>"
            if response.expires_at else "\n⏰ Бессрочный"
        )

        await safe_edit(
            status_msg,
            (
                f"✅ Код приглашения создан!\n"
                f"🔑 <code>{response.invite_code}</code>"
                f"{expires_text}"
            ),
            parse_mode="HTML",
        )
    else:
        await safe_edit(status_msg, get_error_text(response.error_code))