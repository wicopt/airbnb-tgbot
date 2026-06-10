class AuthRepository:
    def __init__(self, db):
        self.db = db

    async def get_user_group(self, user_id: str) -> str | None:
        return await self.db.fetchval("""
            SELECT group_id
            FROM auth.user_groups
            WHERE user_id = $1
        """, user_id)
