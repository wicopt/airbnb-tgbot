import json
import logging
from aio_pika import IncomingMessage

from messaging.dto import GetStatisticsResponse, GetRoiResponse
from messaging.pending import resolve_future

logger = logging.getLogger(__name__)


async def handle_get_statistics_response(message: IncomingMessage):
    async with message.process():
        data = json.loads(message.body)
        response = GetStatisticsResponse.from_dict(data)
        logger.info("GetStatisticsResponse: correlation_id=%s", response.correlation_id)
        resolve_future(response.correlation_id, response)


async def handle_get_roi_response(message: IncomingMessage):
    async with message.process():
        data = json.loads(message.body)
        response = GetRoiResponse.from_dict(data)
        logger.info("GetRoiResponse: correlation_id=%s", response.correlation_id)
        resolve_future(response.correlation_id, response)