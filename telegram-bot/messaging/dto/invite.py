import json
from dataclasses import dataclass, field
from typing import Optional
from .base import generate_correlation_id


# ---- Requests ----

@dataclass
class UseInviteRequest:
    user_id: str
    invite_code: str
    correlation_id: str = field(default_factory=generate_correlation_id)

    def to_json(self) -> bytes:
        return json.dumps({
            "userId": self.user_id,
            "inviteCode": self.invite_code,
            "correlationId": self.correlation_id,
        }).encode()


@dataclass
class GenerateInviteRequest:
    user_id: str
    group_id: Optional[str] = None
    expires_in_hours: Optional[int] = None
    correlation_id: str = field(default_factory=generate_correlation_id)

    def to_json(self) -> bytes:
        data = {
            "userId": self.user_id,
            "correlationId": self.correlation_id,
        }
        if self.group_id is not None:
            data["groupId"] = self.group_id
        if self.expires_in_hours is not None:
            data["expiresInHours"] = self.expires_in_hours
        return json.dumps(data).encode()


# ---- Responses ----

@dataclass
class UseInviteResponse:
    success: bool
    correlation_id: str
    user_id: Optional[str] = None
    group_id: Optional[str] = None
    error_code: Optional[str] = None

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            success=data["success"],
            correlation_id=data["correlationId"],
            user_id=data.get("userId"),
            group_id=data.get("groupId"),
            error_code=data.get("errorCode"),
        )


@dataclass
class GenerateInviteResponse:
    success: bool
    correlation_id: str
    invite_code: Optional[str] = None
    group_id: Optional[str] = None
    expires_at: Optional[str] = None
    error_code: Optional[str] = None

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            success=data["success"],
            correlation_id=data["correlationId"],
            invite_code=data.get("inviteCode"),
            group_id=data.get("groupId"),
            expires_at=data.get("expiresAt"),
            error_code=data.get("errorCode"),
        )