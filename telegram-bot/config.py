from dataclasses import dataclass
from dotenv import load_dotenv
import os

load_dotenv()

@dataclass
class Config:
    bot_token: str
    rabbitmq_url: str
    db_url: str

def load_config() -> Config:
    return Config(
        bot_token=os.getenv("BOT_TOKEN"),
        rabbitmq_url=os.getenv("RABBITMQ_URL"),
        db_url=os.getenv("DATABASE_URL"),
        
    )