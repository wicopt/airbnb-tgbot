import asyncio
import logging
from datetime import date

from aiogram import Router
from aiogram.filters import Command
from aiogram.types import Message

from messaging.dto import CreatePaymentRequest
from messaging.pending import create_future, wait_for_result
from messaging.producer import AuthProducer
from handlers.errors import get_error_text

router = Router()
logger = logging.getLogger(__name__)


async def safe_edit(message: Message, text: str, **kwargs):
    try:
        return await message.edit_text(text, **kwargs)
    except Exception:
        return await message.answer(text, **kwargs)


@router.message(Command("pay"))
async def cmd_create_payment(
    message: Message,
    producer: AuthProducer,
    group_id: str | None,
):
    if not group_id:
        await message.answer("❌ Ты не в группе. Используй /join <код>")
        return

    args = message.text.split()

    if len(args) < 4:
        await message.answer(
            "Использование:\n"
            "<code>/pay &lt;room_number&gt; &lt;category&gt; &lt;amount&gt; [currency] [YYYY-MM-DD]</code>\n\n"
            "Примеры:\n"
            "<code>/pay 2807 rent 50000 THB 2024-03-01</code>\n"
            "<code>/pay 2807 cleaning -75</code>",
            parse_mode="HTML",
        )
        return

    room_number = args[1]
    category = args[2]

    try:
        amount = float(args[3])
    except ValueError:
        await message.answer("❌ Некорректная сумма")
        return

    currency = "USD"
    payment_date = date.today().isoformat()

    for arg in args[4:]:
        if len(arg) == 3 and arg.isalpha():
            currency = arg.upper()
        else:
            try:
                date.fromisoformat(arg)
                payment_date = arg
            except ValueError:
                await message.answer(
                    f"❌ Неизвестный параметр: <code>{arg}</code>",
                    parse_mode="HTML",
                )
                return

    request = CreatePaymentRequest(
        group_id=group_id,
        room_number=room_number,
        category=category,
        amount=amount,
        payment_date=payment_date,
        currency=currency,
    )

    create_future(request.correlation_id)

    status_msg = await message.answer("⏳ Сохраняю платёж 💾")

    await producer.send_create_payment(request)

    try:
        response = await wait_for_result(request.correlation_id, timeout=10.0)
    except asyncio.TimeoutError:
        await safe_edit(status_msg, "⌛ Сервис не ответил")
        return

    if response.success:
        sign = "+" if response.amount >= 0 else ""
        kind = "Доход" if response.amount >= 0 else "Расход"

        conversion_text = ""
        if response.original_currency and response.original_currency != "USD":
            orig_sign = "+" if response.original_amount >= 0 else ""
            conversion_text = (
                f"\n💱 Оригинал: <b>{orig_sign}{response.original_amount:,.2f} {response.original_currency}</b>"
            )

        await safe_edit(
            status_msg,
            f"✅ Платёж сохранён\n\n"
            f"{kind}: <b>{sign}{response.amount:,.2f} USD</b>{conversion_text}\n"
            f"Квартира: <b>{response.room_number}</b>\n"
            f"Категория: <b>{response.category}</b>\n"
            f"Дата: <b>{response.payment_date}</b>",
            parse_mode="HTML",
        )
    else:
        error_messages = {
            "EXCHANGE_RATE_UNAVAILABLE": "Не удалось получить курс валюты",
            "SAVE_FAILED": "Ошибка сохранения платежа",
        }

        await safe_edit(
            status_msg,
            error_messages.get(response.error_code, get_error_text(response.error_code)),
        )