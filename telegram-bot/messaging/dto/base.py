import json
import uuid
from dataclasses import dataclass, field


def generate_correlation_id() -> str:
    return str(uuid.uuid4())


@dataclass
class BaseRequest:
    correlation_id: str = field(default_factory=generate_correlation_id)

    def to_json(self) -> bytes:
        return json.dumps(self.to_dict()).encode()

    def to_dict(self) -> dict:
        raise NotImplementedError