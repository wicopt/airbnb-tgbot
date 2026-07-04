     
from dataclasses import dataclass, field
from typing import List, Optional


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


@dataclass
class SavedPaymentDto:
    payment_id: str
    group_id: str
    room_number: str
    amount: float
    payment_date: str
    category: str
    already_exists: bool = False

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            payment_id=data["payment_id"],
            group_id=data["group_id"],
            room_number=data["room_number"],
            amount=data["amount"],
            payment_date=data["payment_date"],
            category=data["category"],
            already_exists=data.get("alreadyExists", False),
        )


@dataclass
class PaymentProcessedEvent:
    group_id: str
    saved_count: int
    total_count: int
    saved_payments: List[SavedPaymentDto] = field(default_factory=list)
    errors: List[dict] = field(default_factory=list)
    processed_at: Optional[str] = None

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            group_id=data["groupId"],
            saved_count=data["savedCount"],
            total_count=data["totalCount"],
            saved_payments=[SavedPaymentDto.from_dict(p) for p in data.get("savedPayments", [])],
            errors=data.get("errors", []),
            processed_at=data.get("processedAt"),
        )