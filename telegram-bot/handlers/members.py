# handlers/members.py

import asyncio
import logging
from aiogram import Router, Bot
from aiogram.filters import Command
from aiogram.types import Message

from messaging.dto import GetMembersRequest, KickMemberRequest
from messaging.pending import create_future, wait_for_result
from messaging.producer import AuthProducer
from handlers.errors import get_error_text

router = Router()
logger = logging.getLogger(__name__)

ROLE_EMOJI = {
    "admin": "👑",
    "member": "👤",
}


async def safe_edit(msg: Message, text: str, **kwargs):
    try:
        return await msg.edit_text(text, **kwargs)
    except Exception as e:
        logger.warning(f"edit_text failed: {e}")
        return None


@router.message(Command("members"))
async def cmd_members(message: Message, producer: AuthProducer, bot: Bot):
    request = GetMembersRequest(
        requester_id=str(message.from_user.id),
        group_id=None,
    )

    create_future(request.correlation_id)

    status_msg = await message.answer("⏳ Получаю список участников...")

    await producer.send_get_members(request)

    try:
        response = await wait_for_result(request.correlation_id, timeout=10.0)
    except asyncio.TimeoutError:
        await safe_edit(status_msg, "⌛ Сервис не ответил. Попробуй позже.")
        return
    except KeyError:
        await safe_edit(status_msg, "❌ Ошибка связи с сервисом.")
        return

    if not response.success:
        await safe_edit(status_msg, get_error_text(response.error_code))
        return

    if not response.members:
        await safe_edit(status_msg, "👥 Пока нет участников.")
        return

    lines = [f"Участники :\n"]

    for member in response.members:
        emoji = ROLE_EMOJI.get(member.role, "👤")

        try:
            user = await bot.get_chat(member.user_id)
            full_name = " ".join(
                part for part in [user.first_name, user.last_name] if part
            ) or "Без имени"
        except Exception:
            full_name = "Неизвестный пользователь"

        lines.append(
            f"{emoji} {full_name} "
            f"(<code>{member.user_id}</code>) — {member.role}"
        )

    await safe_edit(status_msg, "\n".join(lines), parse_mode="HTML")


@router.message(Command("kick"))
async def cmd_kick(message: Message, producer: AuthProducer):
    args = message.text.split()
    if len(args) < 2:
        await message.answer("Использование: /kick <user_id>")
        return

    request = KickMemberRequest(
        requester_id=str(message.from_user.id),
        target_user_id=args[1].strip(),
        group_id=None,
    )

    create_future(request.correlation_id)

    status_msg = await message.answer("⏳ Обрабатываю...")

    await producer.send_kick_member(request)

    try:
        response = await wait_for_result(request.correlation_id, timeout=10.0)
    except asyncio.TimeoutError:
        await safe_edit(status_msg, "⌛ Сервис не ответил. Попробуй позже.")
        return
    except KeyError:
        await safe_edit(status_msg, "❌ Ошибка связи с сервисом.")
        return

    if response.success:
        await safe_edit(
            status_msg,
            f"✅ Пользователь <code>{response.kicked_user_id}</code> "
            f"исключён.",
            parse_mode="HTML",
        )
    else:
        await safe_edit(status_msg, get_error_text(response.error_code))