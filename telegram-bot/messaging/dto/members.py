import json
from dataclasses import dataclass, field
from typing import Optional, List
from .base import generate_correlation_id


# ---- Requests ----

@dataclass
class GetMembersRequest:
    requester_id: str
    group_id: Optional[str] = None
    correlation_id: str = field(default_factory=generate_correlation_id)

    def to_json(self) -> bytes:
        data = {
            "requesterId": self.requester_id,
            "correlationId": self.correlation_id,
        }
        if self.group_id is not None:
            data["groupId"] = self.group_id
        return json.dumps(data).encode()


@dataclass
class KickMemberRequest:
    requester_id: str
    target_user_id: str
    group_id: Optional[str] = None
    correlation_id: str = field(default_factory=generate_correlation_id)

    def to_json(self) -> bytes:
        data = {
            "requesterId": self.requester_id,
            "targetUserId": self.target_user_id,
            "correlationId": self.correlation_id,
        }
        if self.group_id is not None:
            data["groupId"] = self.group_id
        return json.dumps(data).encode()


# ---- DTO ----

@dataclass
class MemberDto:
    user_id: str
    role: str
    joined_at: str

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            user_id=data["userId"],
            role=data["role"],
            joined_at=data["joinedAt"],
        )


# ---- Responses ----

@dataclass
class GetMembersResponse:
    success: bool
    correlation_id: str
    group_id: Optional[str] = None
    members: List[MemberDto] = field(default_factory=list)
    error_code: Optional[str] = None

    @classmethod
    def from_dict(cls, data: dict):
        members_data = data.get("members") or []
        members = [MemberDto.from_dict(m) for m in members_data]

        return cls(
            success=data["success"],
            correlation_id=data["correlationId"],
            group_id=data.get("groupId"),
            members=members,
            error_code=data.get("errorCode"),
        )


@dataclass
class KickMemberResponse:
    success: bool
    correlation_id: str
    group_id: Optional[str] = None
    kicked_user_id: Optional[str] = None
    error_code: Optional[str] = None

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            success=data["success"],
            correlation_id=data["correlationId"],
            group_id=data.get("groupId"),
            kicked_user_id=data.get("kickedUserId"),
            error_code=data.get("errorCode"),
        )