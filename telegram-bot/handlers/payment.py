import asyncio
import logging
from datetime import date

from aiogram import Router, F
from aiogram.filters import Command
from aiogram.fsm.context import FSMContext
from aiogram.types import Message, CallbackQuery

from states.payment import PaymentStates
from keyboards.payment import rooms_keyboard, categories_keyboard, currency_keyboard, date_keyboard, confirm_keyboard, type_keyboard
from services.paymentService import fetch_categories, fetch_rooms, create_category
from messaging.dto.payment import CreatePaymentRequest
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
async def cmd_create_payment(message: Message, state: FSMContext, group_id: str | None):
    if not group_id:
        await message.answer("❌ Ты не в группе. Используй /join <код>")
        return

    rooms = await fetch_rooms(group_id)
    if not rooms:
        await message.answer("❌ Квартиры не найдены")
        return

    await state.update_data(group_id=group_id)
    await state.set_state(PaymentStates.waiting_room)
    await message.answer("Выбери квартиру:", reply_markup=rooms_keyboard(rooms))


@router.callback_query(F.data.startswith("room:"), PaymentStates.waiting_room)
async def step_room(callback: CallbackQuery, state: FSMContext):
    room_number = callback.data.split(":")[1]
    is_shared = room_number == "shared"

    await state.update_data(
        room_number=None if is_shared else room_number,
        is_shared=is_shared
    )

    data = await state.get_data()
    categories = await fetch_categories(data["group_id"], shared=is_shared)

    room_label = "Общий расход" if is_shared else f"Квартира {room_number}"
    await state.set_state(PaymentStates.waiting_category)

    if not categories:
        await state.set_state(PaymentStates.waiting_new_category)
        await callback.message.edit_text(
            f"{room_label}\n\nКатегорий пока нет. Введи название новой:"
        )
        await callback.answer()
        return

    await callback.message.edit_text(
        f"{room_label}\n\nВыбери категорию:",
        reply_markup=categories_keyboard(categories)
    )
    await callback.answer()


@router.callback_query(F.data.startswith("cat:"), PaymentStates.waiting_category)
async def step_category(callback: CallbackQuery, state: FSMContext):
    parts = callback.data.split(":", 2)

    if parts[1] == "new":
        await state.set_state(PaymentStates.waiting_new_category)
        await callback.message.edit_text("Введи название новой категории:")
        await callback.answer()
        return

    _, category_id, category_name = parts
    await state.update_data(category_id=int(category_id), category_name=category_name)
    await state.set_state(PaymentStates.waiting_type)
    await callback.message.edit_text(
        f"Категория: <b>{category_name}</b>\n\nТип операции:",
        parse_mode="HTML",
        reply_markup=type_keyboard()
    )
    await callback.answer()


@router.callback_query(F.data.startswith("type:"), PaymentStates.waiting_type)
async def step_type(callback: CallbackQuery, state: FSMContext):
    payment_type = callback.data.split(":")[1]  # "income" или "expense"
    await state.update_data(payment_type=payment_type)
    await state.set_state(PaymentStates.waiting_amount)
    await callback.message.edit_text(
        "Введи сумму (только положительное число):",
        parse_mode="HTML"
    )
    await callback.answer()


@router.message(PaymentStates.waiting_new_category)
async def step_new_category(message: Message, state: FSMContext):
    name = message.text.strip()

    if len(name) < 2:
        await message.answer("❌ Слишком короткое название")
        return

    data = await state.get_data()
    is_shared = data.get("is_shared", False)

    logger.info(
        "Creating category: group=%s, name=%s, shared=%s",
        data["group_id"],
        name,
        is_shared
    )

    category = await create_category(data["group_id"], name, is_shared)

    await state.update_data(category_id=category["categoryId"], category_name=category["categoryName"])
    await state.set_state(PaymentStates.waiting_type)
    await message.answer(
        f"Категория <b>{category['categoryName']}</b> создана\n\nТип операции:",
        parse_mode="HTML",
        reply_markup=type_keyboard()
    )


@router.message(PaymentStates.waiting_amount)
async def step_amount(message: Message, state: FSMContext):
    try:
        amount = float(message.text.strip())
    except ValueError:
        await message.answer("❌ Некорректная сумма, попробуй снова:")
        return

    await state.update_data(amount=amount)
    await state.set_state(PaymentStates.waiting_currency)
    await message.answer("Выбери валюту:", reply_markup=currency_keyboard())


@router.callback_query(F.data.startswith("cur:"), PaymentStates.waiting_currency)
async def step_currency_button(callback: CallbackQuery, state: FSMContext):
    currency = callback.data.split(":")[1]
    await state.update_data(currency=currency)
    await state.set_state(PaymentStates.waiting_date)
    await callback.message.edit_text(
        f"Валюта: <b>{currency}</b>\n\nВыбери дату:",
        parse_mode="HTML",
        reply_markup=date_keyboard()
    )
    await callback.answer()


@router.message(PaymentStates.waiting_currency)
async def step_currency_text(message: Message, state: FSMContext):
    currency = message.text.strip().upper()
    if len(currency) != 3 or not currency.isalpha():
        await message.answer("❌ Некорректная валюта (например: USD, THB, EUR)")
        return
    await state.update_data(currency=currency)
    await state.set_state(PaymentStates.waiting_date)
    await message.answer("Выбери дату:", reply_markup=date_keyboard())


@router.callback_query(F.data == "date:today", PaymentStates.waiting_date)
async def step_date_today(callback: CallbackQuery, state: FSMContext):
    await state.update_data(payment_date=date.today().isoformat())
    await callback.answer()
    await show_confirm(callback.message, state)


@router.message(PaymentStates.waiting_date)
async def step_date_text(message: Message, state: FSMContext):
    try:
        date.fromisoformat(message.text.strip())
    except ValueError:
        await message.answer("❌ Некорректная дата, формат: YYYY-MM-DD")
        return
    await state.update_data(payment_date=message.text.strip())
    await show_confirm(message, state)


async def show_confirm(msg: Message, state: FSMContext):
    data = await state.get_data()

    amount = data["amount"]
    if data.get("payment_type") == "expense":
        amount = -abs(amount)
    else:
        amount = abs(amount)

    room_label = "Общий" if data.get("is_shared") else f"Квартира {data.get('room_number')}"
    sign = "+" if amount >= 0 else ""
    kind = "Доход" if amount >= 0 else "Расход"

    text = (
        f"Проверь платёж:\n\n"
        f"{kind}: <b>{sign}{amount:,.2f} {data['currency']}</b>\n"
        f"Квартира: <b>{room_label}</b>\n"
        f"Категория: <b>{data['category_name']}</b>\n"
        f"Дата: <b>{data['payment_date']}</b>"
    )

    await state.set_state(PaymentStates.waiting_confirm)
    await safe_edit(msg, text, parse_mode="HTML", reply_markup=confirm_keyboard())
    
    
@router.callback_query(F.data == "pay:confirm", PaymentStates.waiting_confirm)
async def step_confirm(callback: CallbackQuery, state: FSMContext, producer: AuthProducer):
    await callback.message.edit_text("Сохраняю платёж...")
    await callback.answer()
    await send_payment(callback.message, state, producer)


@router.callback_query(F.data == "pay:cancel")
async def cancel_payment(callback: CallbackQuery, state: FSMContext):
    await state.clear()
    await callback.message.edit_text("Платёж отменён")
    await callback.answer()


async def send_payment(status_msg: Message, state: FSMContext, producer: AuthProducer):
    data = await state.get_data()
    await state.clear()

    # Применяем знак в зависимости от типа
    amount = data["amount"]
    if data.get("payment_type") == "expense":
        amount = -abs(amount)
    else:
        amount = abs(amount)

    request = CreatePaymentRequest(
        group_id=data["group_id"],
        room_number=data.get("room_number"),
        category_id=data["category_id"],
        amount=amount,          # <- вместо data["amount"]
        payment_date=data["payment_date"],
        currency=data["currency"],
    )

    create_future(request.correlation_id)
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
                f"\nОригинал: <b>{orig_sign}{response.original_amount:,.2f} {response.original_currency}</b>"
            )

        room_label = "Общий" if data.get(
            "is_shared") else f"<b>{response.room_number}</b>"

        await safe_edit(
            status_msg,
            f"✅ Платёж сохранён\n\n"
            f"{kind}: <b>{sign}{response.amount:,.2f} USD</b>{conversion_text}\n"
            f"Квартира: {room_label}\n"
            f"Категория: <b>{response.category_name}</b>\n"
            f"Дата: <b>{response.payment_date}</b>",
            parse_mode="HTML",
        )
    else:
        error_messages = {
            "EXCHANGE_RATE_UNAVAILABLE": "Не удалось получить курс валюты",
            "CATEGORY_NOT_FOUND": "Категория не найдена",
            "SAVE_FAILED": "Ошибка сохранения платежа",
        }
        await safe_edit(
            status_msg,
            error_messages.get(response.error_code,
                               get_error_text(response.error_code)),
        )
