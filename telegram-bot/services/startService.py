class StartService:
    
    @staticmethod
    def get_welcome_message() -> str:
        """Формирует приветственное сообщение"""
        return (
            "👋 Добро пожаловать в Airbnb бот!\n\n"
            "Выберите действие с помощью кнопок ниже:"
        )