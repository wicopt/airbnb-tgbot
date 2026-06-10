# handlers/statistics.py
import asyncio
import logging
from aiogram import Router
from aiogram.filters import Command
from aiogram.types import Message

from messaging.dto import GetStatisticsRequest, GetRoiRequest
from messaging.pending import create_future, wait_for_result
from messaging.producer import AuthProducer

router = Router()
logger = logging.getLogger(__name__)


async def safe_edit(msg: Message, text: str, **kwargs):
    try:
        return await msg.edit_text(text, **kwargs)
    except Exception as e:
        logger.warning(f"edit_text failed: {e}")
        return None


@router.message(Command("stats"))
async def cmd_statistics(message: Message, producer: AuthProducer, group_id: str):
    """
    /stats                          — ROI по всей группе
    /stats <room_number>            — статистика квартиры
    /stats <room_number> <from> <to> — статистика за период
    """
    args = message.text.split()

    # Без параметров — ROI
    if len(args) == 1:
        request = GetRoiRequest(group_id=group_id)
        create_future(request.correlation_id)
        status_msg = await message.answer("⏳ Считаю рентабельность...")
        await producer.send_get_roi(request)

        try:
            response = await wait_for_result(request.correlation_id, timeout=10.0)
        except asyncio.TimeoutError:
            await safe_edit(status_msg, "⌛ Сервис не ответил. Попробуй позже.")
            return

        if not response.rooms:
            await safe_edit(status_msg, "Квартиры не найдены.")
            return

        lines = [f"<b> Общая статистика </b>\n"]
        for room in response.rooms:
            sign = "+" if room.net_profit >= 0 else ""
            roi_sign = "+" if room.roi_percent >= 0 else ""
            lines.append(
                f"<b> 💸{room.message_name}</b>\n"
                f"  Куплена за: {room.purchase_price:,.2f}\n"
                f"  Доходы: {room.total_income:,.2f}\n"
                f"  Расходы: {room.total_expenses:,.2f}\n"
                f"  Прибыль: {sign}{room.net_profit:,.2f}\n"
                f"  ROI: <b>{roi_sign}{room.roi_percent:.2f}%</b>\n"
            )

        await safe_edit(status_msg, "\n".join(lines), parse_mode="HTML")
        return

    # С параметрами — статистика квартиры
    if len(args) < 2:
        await message.answer(
            "Использование:\n"
            "/stats — ROI по группе\n"
            "/stats <room_number> — статистика квартиры\n"
            "/stats <room_number> <date_from> <date_to> — за период\n\n"
            "Пример: /stats 2605 2026-01-01 2026-02-20"
        )
        return

    room_number = args[1]
    date_from = args[2] if len(args) >= 3 else None
    date_to = args[3] if len(args) >= 4 else None

    request = GetStatisticsRequest(
        group_id=group_id,
        room_number=room_number,
        date_from=date_from,
        date_to=date_to,
    )
    create_future(request.correlation_id)
    status_msg = await message.answer("⏳ Загружаю статистику...")
    await producer.send_get_statistics(request)

    try:
        response = await wait_for_result(request.correlation_id, timeout=10.0)
    except asyncio.TimeoutError:
        await safe_edit(status_msg, "⌛ Сервис не ответил. Попробуй позже.")
        return

    period_text = ""
    if date_from and date_to:
        period_text = f"\nПериод: {date_from} — {date_to}"
    elif date_from:
        period_text = f"\nС: {date_from}"

    sign = "+" if response.net_profit >= 0 else ""
    text = (
        f"<b>💸 Статистика квартиры {response.room_number}</b>{period_text}\n\n"
        f"Доходы: <b>{response.total_income:,.2f}</b>\n"
        f"Расходы: <b>{response.total_expenses:,.2f}</b>\n"
        f"Прибыль: <b>{sign}{response.net_profit:,.2f}</b>\n\n"
        f"Платежей: <b>{len(response.payments)}</b>"
    )
    await safe_edit(status_msg, text, parse_mode="HTML")