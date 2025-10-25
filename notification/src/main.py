from fastapi import FastAPI, HTTPException, Depends
from pydantic import BaseModel, EmailStr
from typing import List, Union, Optional
import os
from tg_sender import TelegramBot
from dotenv import load_dotenv 

load_dotenv()

import sys
import os
sys.path.append(os.path.dirname(__file__))
from email_sender import EmailSender




class EmailRequest(BaseModel):
    to_emails: Union[str, List[EmailStr]]
    subject: str
    message: str
    cc_emails: Union[str, List[EmailStr], None] = None


class TransactionAlertRequest(BaseModel):
    to_emails: Union[str, List[EmailStr], None] = None  
    transaction_id: str
    account: str
    amount: float
    ml_probability: float
    triggered_rules: List[str]
    cc_emails: Union[str, List[EmailStr], None] = None
    user_ids: Union[str, List[str], None] = None  


class TelegramAlertRequest(BaseModel):
    transaction_id: str
    account: str
    amount: float
    ml_probability: float
    triggered_rules: List[str]
    user_ids: Union[str, List[str], None] = None  

def get_telegram_bot():
    """
    🔌 ФАБРИКА ДЛЯ СОЗДАНИЯ TELEGRAM BOT
    """
    bot_token = os.getenv("BOT_TOKEN")
    
    if not bot_token:
        raise ValueError("❌ BOT_TOKEN must be set in .env file")
    
    telegram_bot = TelegramBot(bot_token=bot_token)
    print("✅ Telegram bot created successfully")
    return telegram_bot

def get_email_sender():
    smtp_server = os.getenv("SMTP_SERVER", "smtp.gmail.com")
    smtp_port = int(os.getenv("SMTP_PORT", "587"))
    email_login = os.getenv("EMAIL_LOGIN")
    email_password = os.getenv("EMAIL_PASSWORD")
    
    if not email_login or not email_password:
        raise ValueError("❌ EMAIL_LOGIN and EMAIL_PASSWORD must be set in .env file")
    
    email_sender = EmailSender(
        smtp_server=smtp_server,
        smtp_port=smtp_port,
        login=email_login,
        password=email_password
    )
    
    print("✅ Email sender created successfully")
    return email_sender

app = FastAPI(
    title="Simple Email Service",
    description="Простой сервис отправки email уведомлений и ТГ ботов",
    version="1.0.0"
)

@app.post("/send-email")
async def send_email(
    request: EmailRequest,
    email_sender: EmailSender = Depends(get_email_sender)
):
    print(f"📨 Received request to send email to: {request.to_emails}")
    
    success = email_sender.send_email(
        to_emails=request.to_emails,
        subject=request.subject,
        message=request.message,
        cc_emails=request.cc_emails
    )
    
    if success:
        return {
            "status": "success", 
            "message": "Email отправлен",
            "to": request.to_emails,
            "cc": request.cc_emails
        }
    else:
        raise HTTPException(
            status_code=500, 
            detail="Ошибка отправки email"
        )

@app.post("/send-transaction-alert")
async def send_transaction_alert(
    request: TransactionAlertRequest,
    email_sender: EmailSender = Depends(get_email_sender)
):
    print(f"🚨 Received transaction alert for: {request.transaction_id}")
    
    success = email_sender.send_transaction_alert(
        to_emails=request.to_emails,
        transaction_id=request.transaction_id,
        account=request.account,
        amount=request.amount,
        ml_probability=request.ml_probability,
        triggered_rules=request.triggered_rules,
        cc_emails=request.cc_emails
    )
    
    if success:
        return {
            "status": "success",
            "message": "Уведомление о транзакции отправлено",
            "transaction_id": request.transaction_id,
            "to": request.to_emails,
            "cc": request.cc_emails
        }
    else:
        raise HTTPException(
            status_code=500, 
            detail="Ошибка отправки уведомления"
        )


@app.post("/send-telegram-alert")
async def send_telegram_alert(
    request: TelegramAlertRequest, 
    tg_sender: TelegramBot = Depends(get_telegram_bot)
):
    """
    🔔 ENDPOINT ДЛЯ TELEGRAM УВЕДОМЛЕНИЙ О ТРАНЗАКЦИЯХ
    """
    print(f"🔔 Received Telegram alert for transaction: {request.transaction_id}")
    print(f"🔔 User IDs: {request.user_ids}")
    
    success = tg_sender.send_transaction_alert(
        transaction_id=request.transaction_id,
        account=request.account,
        amount=request.amount,
        ml_probability=request.ml_probability,
        triggered_rules=request.triggered_rules,
        user_ids=request.user_ids
    )
    
    if success:
        return {
            "status": "success",
            "message": "Telegram уведомление отправлено",
            "transaction_id": request.transaction_id,
            "user_ids": request.user_ids
        }
    else:
        raise HTTPException(
            status_code=500, 
            detail="Ошибка отправки Telegram уведомления"
        )

@app.get("/")
async def root():
    return {
        "message": "✅ Notification Service is running!",
        "docs": "Visit /docs for API documentation",
        "endpoints": {
            "send_email": "POST /send-email",
            "send_transaction_alert": "POST /send-transaction-alert",
            "send_telegram_alert": "POST /send-telegram-alert"
        }
    }

@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "notification-api"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)