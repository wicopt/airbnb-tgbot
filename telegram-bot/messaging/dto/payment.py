import json
from dataclasses import dataclass, field
from typing import Optional, List
from .base import generate_correlation_id

@dataclass
class CreatePaymentRequest:
    group_id: str
    room_number: str
    category: str
    amount: float
    payment_date: str
    currency: str = "USD"    # добавить
    correlation_id: str = field(default_factory=generate_correlation_id)

    def to_json(self) -> bytes:
        return json.dumps({
            "groupId": self.group_id,
            "roomNumber": self.room_number,
            "category": self.category,
            "amount": self.amount,
            "paymentDate": self.payment_date,
            "currency": self.currency,         # добавить
            "correlationId": self.correlation_id,
        }).encode()


@dataclass
class CreatePaymentResponse:
    success: bool
    correlation_id: str
    payment_id: Optional[str] = None
    group_id: Optional[str] = None
    room_number: Optional[str] = None
    category: Optional[str] = None
    amount: Optional[float] = None
    original_amount: Optional[float] = None    # добавить
    original_currency: Optional[str] = None    # добавить
    payment_date: Optional[str] = None
    error_code: Optional[str] = None

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            success=data["success"],
            correlation_id=data["correlationId"],
            payment_id=data.get("paymentId"),
            group_id=data.get("groupId"),
            room_number=data.get("roomNumber"),
            category=data.get("category"),
            amount=float(data["amount"]) if data.get("amount") is not None else None,
            original_amount=float(data["originalAmount"]) if data.get("originalAmount") is not None else None,
            original_currency=data.get("originalCurrency"),
            payment_date=data.get("paymentDate"),
            error_code=data.get("errorCode"),
        )