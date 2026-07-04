import httpx
from config import settings

async def fetch_categories(group_id: str, shared: bool | None = None) -> list[dict]:
    params = {"groupId": group_id}
    if shared is not None:
        params["isShared"] = str(shared).lower()  # "true" или "false"
    
    async with httpx.AsyncClient() as client:
        r = await client.get(
            f"{settings.backend_url}/api/categories/filter",
            params=params
        )
        return r.json()

async def fetch_rooms(group_id: str) -> list[dict]:
    async with httpx.AsyncClient() as client:
        r = await client.get(
            f"{settings.backend_url}/api/rooms",
            params={"groupId": group_id}
        )
        return r.json()  
    
async def create_category(group_id: str, name: str, is_shared: bool) -> dict:
    async with httpx.AsyncClient() as client:
        r = await client.post(
            f"{settings.backend_url}/api/categories",
            json={"categoryName": name, "groupId": group_id, "shared": is_shared}
        )
        return r.json()