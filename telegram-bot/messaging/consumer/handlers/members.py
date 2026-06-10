import json
import logging
from aio_pika import IncomingMessage

from messaging.dto import GetMembersResponse, KickMemberResponse
from messaging.pending import resolve_future

logger = logging.getLogger(__name__)


async def handle_get_members_response(message: IncomingMessage):
    async with message.process():
        data = json.loads(message.body)
        response = GetMembersResponse.from_dict(data)

        logger.info("GetMembersResponse: %s %s",
                    response.correlation_id, response.success)

        resolve_future(response.correlation_id, response)


async def handle_kick_member_response(message: IncomingMessage):
    async with message.process():
        data = json.loads(message.body)
        response = KickMemberResponse.from_dict(data)

        logger.info("KickMemberResponse: %s %s",
                    response.correlation_id, response.success)

        resolve_future(response.correlation_id, response)