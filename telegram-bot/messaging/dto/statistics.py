import json
from dataclasses import dataclass, field
from typing import Optional
from .base import generate_correlation_id


# ---- Requests ----

@dataclass
class GetStatisticsRequest:
    group_id: str
    room_number: str
    date_from: Optional[str] = None  # ISO date: "2024-01-01"
    date_to: Optional[str] = None
    correlation_id: str = field(default_factory=generate_correlation_id)

    def to_json(self) -> bytes:
        data = {
            "groupId": self.group_id,
            "roomNumber": self.room_number,
            "correlationId": self.correlation_id,
        }
        if self.date_from:
            data["from"] = self.date_from
        if self.date_to:
            data["to"] = self.date_to
        return json.dumps(data).encode()


@dataclass
class GetRoiRequest:
    group_id: str
    correlation_id: str = field(default_factory=generate_correlation_id)

    def to_json(self) -> bytes:
        return json.dumps({
            "groupId": self.group_id,
            "correlationId": self.correlation_id,
        }).encode()


# ---- Responses ----

@dataclass
class PaymentDto:
    payment_id: str
    amount: float
    payment_date: str
    category: str

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            payment_id=data["paymentId"],
            amount=data["amount"],
            payment_date=data["paymentDate"],
            category=data["category"],
        )


@dataclass
class GetStatisticsResponse:
    correlation_id: str
    group_id: str
    room_number: str
    total_income: float
    total_expenses: float
    net_profit: float
    payments: list

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            correlation_id=data["correlationId"],
            group_id=data["groupId"],
            room_number=data["roomNumber"],
            total_income=float(data["totalIncome"]),
            total_expenses=float(data["totalExpenses"]),
            net_profit=float(data["netProfit"]),
            payments=[PaymentDto.from_dict(p) for p in data.get("payments", [])],
        )


@dataclass
class RoomRoiDto:
    room_number: str
    message_name: str
    purchase_price: float
    total_income: float
    total_expenses: float
    net_profit: float
    roi_percent: float

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            room_number=data["roomNumber"],
            message_name=data["messageName"],
            purchase_price=float(data["purchasePrice"]),
            total_income=float(data["totalIncome"]),
            total_expenses=float(data["totalExpenses"]),
            net_profit=float(data["netProfit"]),
            roi_percent=float(data["roiPercent"]),
        )


@dataclass
class GetRoiResponse:
    correlation_id: str
    group_id: str
    rooms: list

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            correlation_id=data["correlationId"],
            group_id=data["groupId"],
            rooms=[RoomRoiDto.from_dict(r) for r in data.get("rooms", [])],
        )