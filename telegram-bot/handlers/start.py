# handlers/start.py

from aiogram import Router
from aiogram.filters import Command, CommandStart
from aiogram.types import Message

router = Router()


def build_help_text() -> str:
    return (
        "<b>Доступные команды</b>\n\n"
        "<code>/join &lt;код&gt;</code> — вход в группу\n\n"
        "<code>/invite [часы]</code> — создать код приглашения\n\n"
        "<code>/members</code> — список участников\n\n"
        "<code>/kick &lt;user_id&gt;</code> — исключить участника\n\n"
        "<code>/pay &lt;room&gt; &lt;category&gt; &lt;amount&gt; &lt;currency&gt; &lt;date&gt;</code> — добавить платёж\n\n"
        "<code>/stats</code> — общая статистика\n\n"
        "<code>/stats &lt;room&gt;</code> — статистика квартиры\n\n"
    )


@router.message(CommandStart())
async def cmd_start(message: Message):
    await message.answer(build_help_text(), parse_mode="HTML")


@router.message(Command("help"))
async def cmd_help(message: Message):
    await message.answer(build_help_text(), parse_mode="HTML")