# messaging/consumer/router.py

from messaging.queues import Queues
from .handlers.invite import (
    handle_use_invite_response,
    handle_generate_invite_response,
)
from .handlers.members import (
    handle_get_members_response,
    handle_kick_member_response,
)
from .handlers.events import create_member_joined_handler
from .handlers.statistics import (
    handle_get_statistics_response,
    handle_get_roi_response,
)
from .handlers.payment import handle_create_payment_response
from .handlers.payment_processed import create_payment_processed_handler

def get_queue_handlers(bot, producer):
    return {
        Queues.USE_INVITE_RESPONSE: handle_use_invite_response,
        Queues.GENERATE_INVITE_RESPONSE: handle_generate_invite_response,
        Queues.GET_MEMBERS_RESPONSE: handle_get_members_response,
        Queues.KICK_MEMBER_RESPONSE: handle_kick_member_response,
        Queues.MEMBER_JOINED_EVENT: create_member_joined_handler(bot),
        Queues.GET_STATISTICS_RESPONSE: handle_get_statistics_response,
        Queues.GET_ROI_RESPONSE: handle_get_roi_response,
        Queues.CREATE_PAYMENT_RESPONSE: handle_create_payment_response,
        Queues.PAYMENT_PROCESSED_EVENT: create_payment_processed_handler(bot, producer),
    }
