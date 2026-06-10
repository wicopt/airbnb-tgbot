import logging
from aio_pika import connect_robust, Message, DeliveryMode
from aio_pika.abc import AbstractRobustConnection, AbstractChannel

from messaging.dto import (
    UseInviteRequest, GenerateInviteRequest,
    GetMembersRequest, KickMemberRequest,
    GetStatisticsRequest, GetRoiRequest,
    CreatePaymentRequest,  
)
from messaging.queues import Queues


logger = logging.getLogger(__name__)


class AuthProducer:
    def __init__(self, rabbitmq_url: str):
        self._url = rabbitmq_url
        self._connection: AbstractRobustConnection | None = None
        self._channel: AbstractChannel | None = None

    async def connect(self):
        self._connection = await connect_robust(self._url)
        self._channel = await self._connection.channel()
        logger.info("Producer connected")

    async def close(self):
        if self._connection:
            await self._connection.close()

    async def _publish(self, queue_name: str, payload: bytes):
        message = Message(
            payload,
            content_type="application/json",
            delivery_mode=DeliveryMode.PERSISTENT,
        )
        await self._channel.default_exchange.publish(
            message,
            routing_key=queue_name,
        )

    async def send_use_invite(self, request: UseInviteRequest):
        await self._publish(Queues.USE_INVITE_REQUEST, request.to_json())

    async def send_generate_invite(self, request: GenerateInviteRequest):
        await self._publish(Queues.GENERATE_INVITE_REQUEST, request.to_json())

    async def send_get_members(self, request: GetMembersRequest):
        await self._publish(Queues.GET_MEMBERS_REQUEST, request.to_json())

    async def send_kick_member(self, request: KickMemberRequest):
        await self._publish(Queues.KICK_MEMBER_REQUEST, request.to_json())

    async def send_get_statistics(self, request: GetStatisticsRequest):
        await self._publish(Queues.GET_STATISTICS_REQUEST, request.to_json())

    async def send_get_roi(self, request: GetRoiRequest):
        await self._publish(Queues.GET_ROI_REQUEST, request.to_json())
        
    async def send_create_payment(self, request: CreatePaymentRequest):
        await self._publish(Queues.CREATE_PAYMENT_REQUEST, request.to_json())