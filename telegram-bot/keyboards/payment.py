from aiogram.types import InlineKeyboardMarkup, InlineKeyboardButton


def rooms_keyboard(rooms: list[dict]) -> InlineKeyboardMarkup:
    buttons = [
        [InlineKeyboardButton(
            text=r["messageName"],
            callback_data=f"room:{r['roomNumber']}"
        )]
        for r in rooms
    ]
    
    buttons.append([
        InlineKeyboardButton(text="Общий расход", callback_data="room:shared")
    ])
    return InlineKeyboardMarkup(inline_keyboard=buttons)


def categories_keyboard(categories: list[dict]) -> InlineKeyboardMarkup:
    buttons = [
        [
            InlineKeyboardButton(
                text=c["categoryName"],
                callback_data=f"cat:{c['categoryId']}:{c['categoryName']}"
            )
        ]
        for c in categories
    ]

    buttons.append([
        InlineKeyboardButton(
            text="➕ Новая категория",
            callback_data="cat:new"
        )
    ])

    return InlineKeyboardMarkup(inline_keyboard=buttons)

def currency_keyboard() -> InlineKeyboardMarkup:
    return InlineKeyboardMarkup(inline_keyboard=[[
        InlineKeyboardButton(text="USD", callback_data="cur:USD"),
        InlineKeyboardButton(text="THB", callback_data="cur:THB"),
    ]])

def date_keyboard() -> InlineKeyboardMarkup:
    return InlineKeyboardMarkup(inline_keyboard=[[
        InlineKeyboardButton(text="Сегодня", callback_data="date:today")
    ]])

def type_keyboard():
    return InlineKeyboardMarkup(inline_keyboard=[
        [
            InlineKeyboardButton(text="➕ Доход", callback_data="type:income"),
            InlineKeyboardButton(
                text="➖ Расход", callback_data="type:expense"),
        ]
    ])
    
def confirm_keyboard() -> InlineKeyboardMarkup:
    return InlineKeyboardMarkup(inline_keyboard=[[
        InlineKeyboardButton(text="✅ Подтвердить", callback_data="pay:confirm"),
        InlineKeyboardButton(text="❌ Отменить", callback_data="pay:cancel"),
    ]])