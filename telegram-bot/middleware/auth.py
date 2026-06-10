from typing import Callable, Dict, Any, Awaitable
from aiogram import BaseMiddleware
from aiogram.types import Message

class AuthMiddleware(BaseMiddleware):
    def __init__(self, auth_repo):
        self.auth_repo = auth_repo

    async def __call__(
        self,
        handler: Callable[[Message, Dict[str, Any]], Awaitable[Any]],
        event: Message,
        data: Dict[str, Any]
    ) -> Any:
        user_id = str(event.from_user.id)
        group_id = await self.auth_repo.get_user_group(user_id)

        data["user_id"] = user_id
        data["group_id"] = group_id

        # Пропускаем /join без проверки
        if event.text and event.text.startswith("/join"):
            return await handler(event, data)

        # Все остальные команды — только для авторизованных
        if not group_id:
            await event.answer("⛔ У вас нет доступа. Используйте /join для входа.")
            return

        return await handler(event, data)