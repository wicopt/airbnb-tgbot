# messaging/consumer/__init__.py
from .base import BaseConsumer
from .router import get_queue_handlers
from .consumer import AuthConsumer