from dataclasses import dataclass
from typing import List


@dataclass
class MemberJoinedEvent:
    new_user_id: str
    group_id: str
    admin_ids: List[str]
    joined_at: str

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            new_user_id=data["newUserId"],
            group_id=data["groupId"],
            admin_ids=data["adminIds"],
            joined_at=data["joinedAt"],
        )