import json
import logging
from aio_pika import IncomingMessage

from messaging.dto import UseInviteResponse, GenerateInviteResponse
from messaging.pending import resolve_future

logger = logging.getLogger(__name__)


async def handle_use_invite_response(message: IncomingMessage):
    async with message.process():
        data = json.loads(message.body)
        response = UseInviteResponse.from_dict(data)

        logger.info("UseInviteResponse: %s %s",
                    response.correlation_id, response.success)

        resolve_future(response.correlation_id, response)


async def handle_generate_invite_response(message: IncomingMessage):
    async with message.process():
        data = json.loads(message.body)
        response = GenerateInviteResponse.from_dict(data)

        logger.info("GenerateInviteResponse: %s %s",
                    response.correlation_id, response.success)

        resolve_future(response.correlation_id, response)