# handlers/errors.py

def get_error_text(error_code):
    """Возвращает понятный текст ошибки по коду"""
    
    errors = {
        "INVITE_NOT_FOUND": "❌ Такой код приглашения не найден.",
        "INVITE_ALREADY_USED": "❌ Этот код уже был использован.",
        "INVITE_EXPIRED": "❌ Срок действия кода истёк.",
        "ALREADY_IN_GROUP": "❌ Вы уже состоите в этой группе.",
        "GROUP_NOT_FOUND": "❌ Группа не найдена.",
        "ACCESS_DENIED": "❌ У вас нет прав для этого действия.",
        "USER_NOT_IN_GROUP": "❌ Пользователь не состоит в группе.",
        "UNKNOWN_ERROR": "⚠️ Произошла неизвестная ошибка."
    }
    
    return errors.get(error_code, f"❌ Ошибка: {error_code}")