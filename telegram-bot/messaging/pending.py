import asyncio
import logging
from typing import Any

logger = logging.getLogger(__name__)

_pending: dict[str, asyncio.Future] = {}


def create_future(correlation_id: str) -> asyncio.Future:
    """Создает future для ожидания ответа"""
    try:
        loop = asyncio.get_running_loop()
        future = loop.create_future()
        _pending[correlation_id] = future
        logger.info("Created future for correlation_id=%s, total pending: %d", 
                   correlation_id, len(_pending))
        return future
    except RuntimeError as e:
        logger.error("Failed to create future for %s: %s", correlation_id, e)
        raise


def resolve_future(correlation_id: str, result: Any):
    """Устанавливает результат для future (НЕ удаляем future из словаря!)"""
    future = _pending.get(correlation_id)  # Используем get, а не pop
    if future and not future.done():
        future.set_result(result)
        logger.info("Resolved future for correlation_id=%s, result set", correlation_id)
    elif future and future.done():
        logger.warning("Future for correlation_id=%s is already done", correlation_id)
    else:
        logger.warning(" No pending future for correlation_id=%s, available: %s", 
                      correlation_id, list(_pending.keys()))


def reject_future(correlation_id: str, error: Exception):
    """Устанавливает исключение для future (НЕ удаляем future из словаря!)"""
    future = _pending.get(correlation_id)  # Используем get, а не pop
    if future and not future.done():
        future.set_exception(error)
        logger.info(" Rejected future for correlation_id=%s with error: %s", 
                   correlation_id, error)
    elif future and future.done():
        logger.warning("Future for correlation_id=%s is already done", correlation_id)
    else:
        logger.warning("No pending future for correlation_id=%s", correlation_id)


async def wait_for_result(correlation_id: str, timeout: float = 10.0) -> Any:
    """Ожидает результат по correlation_id и УДАЛЯЕТ future после получения"""
    future = _pending.get(correlation_id)
    if not future:
        error_msg = f"No pending future for correlation_id: {correlation_id}"
        logger.error("%s, available: %s", error_msg, list(_pending.keys()))
        raise KeyError(error_msg)
    
    logger.info("⏳ Waiting for result for correlation_id=%s (timeout=%ss)", 
                correlation_id, timeout)
    
    try:
        result = await asyncio.wait_for(future, timeout=timeout)
        logger.info("Got result for correlation_id=%s", correlation_id)
        return result
    except asyncio.TimeoutError:
        logger.error("Timeout waiting for correlation_id=%s after %ss", 
                    correlation_id, timeout)
        raise
    except Exception as e:
        logger.error("Error waiting for correlation_id=%s: %s", correlation_id, e)
        raise
    finally:
        # Удаляем future только после того, как дождались результата
        _pending.pop(correlation_id, None)
        logger.info("Removed future for correlation_id=%s, remaining: %d", 
                   correlation_id, len(_pending))


def cleanup_future(correlation_id: str):
    """Принудительно удаляет future"""
    _pending.pop(correlation_id, None)
    logger.info("Cleaned up future for correlation_id=%s", correlation_id)


def get_pending_futures() -> list[str]:
    """Возвращает список ожидаемых correlation_id (для отладки)"""
    return list(_pending.keys())