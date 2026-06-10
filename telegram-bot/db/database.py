import asyncpg
import os
from dotenv import load_dotenv

load_dotenv()

class Database:
    def __init__(self):
        self.db_url = os.getenv("DATABASE_URL")
        self.pool = None

    async def connect(self):
        if not self.db_url:
            raise ValueError("DATABASE_URL is not set")

        self.pool = await asyncpg.create_pool(
            self.db_url,
            min_size=1,
            max_size=10
        )

    async def disconnect(self):
        if self.pool:
            await self.pool.close()

    async def fetchrow(self, query, *args):
        async with self.pool.acquire() as conn:
            return await conn.fetchrow(query, *args)

    async def fetch(self, query, *args):
        async with self.pool.acquire() as conn:
            return await conn.fetch(query, *args)

    async def execute(self, query, *args):
        async with self.pool.acquire() as conn:
            return await conn.execute(query, *args)
        
    async def fetchval(self, query, *args):
        async with self.pool.acquire() as conn:
            return await conn.fetchval(query, *args)