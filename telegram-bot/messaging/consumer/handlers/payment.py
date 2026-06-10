import json
import logging
from aio_pika import IncomingMessage

from messaging.dto import CreatePaymentResponse
from messaging.pending import resolve_future

logger = logging.getLogger(__name__)


async def handle_create_payment_response(message: IncomingMessage):
    async with message.process():
        data = json.loads(message.body)
        response = CreatePaymentResponse.from_dict(data)
        logger.info("CreatePaymentResponse: correlation_id=%s success=%s",
                    response.correlation_id, response.success)
        resolve_future(response.correlation_id, response)