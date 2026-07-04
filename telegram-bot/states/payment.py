from aiogram.fsm.state import State, StatesGroup

class PaymentStates(StatesGroup):
    waiting_room = State()
    waiting_category = State()
    waiting_type = State()  
    waiting_amount = State()
    waiting_currency = State()
    waiting_date = State()
    waiting_confirm = State()
    waiting_new_category = State()