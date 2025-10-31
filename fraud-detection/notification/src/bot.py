import logging
from aiogram import Bot, Dispatcher, types
from aiogram.enums import ParseMode
from aiogram.filters import Command
from aiogram.client.default import DefaultBotProperties
import asyncio
import os

from dotenv import load_dotenv 

load_dotenv()

BOT_TOKEN = os.getenv("BOT_TOKEN")

if not BOT_TOKEN:
    raise ValueError("BOT_TOKEN не установлен")

bot = Bot(
    token=BOT_TOKEN,
    default=DefaultBotProperties(parse_mode=ParseMode.HTML)
)
dp = Dispatcher()

user_states = {}

@dp.message(Command("start"))
async def cmd_start(message: types.Message):
    user_id = message.from_user.id
    first_name = message.from_user.first_name
    username = message.from_user.username
    
    user_states[user_id] = True
    
    response = (
        f"👋 Привет, {first_name}!\n\n"
        f"📋 Ваши данные:\n"
        f"🆔 ID: <code>{user_id}</code>\n"
    )
    
    await message.answer(response)

@dp.message()
async def other_messages(message: types.Message):
    user_id = message.from_user.id

    if user_id not in user_states:
        await message.answer("⚠Пожалуйста, сначала нажмите /start чтобы активировать бота")
    else:
        await message.answer("Нажмите /start чтобы узнать свой ID")

async def main():
    try:

        await bot.delete_webhook(drop_pending_updates=True)
        
        await asyncio.sleep(1)
        
        await dp.start_polling(bot, skip_updates=True)
        
    except Exception as e:
        print(f"Ошибка: {e}")
    finally:
        await bot.session.close()

if __name__ == "__main__":
    asyncio.run(main())