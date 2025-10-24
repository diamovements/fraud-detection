from fastapi import FastAPI, HTTPException, Depends
from pydantic import BaseModel, EmailStr
from typing import List, Union
import os
from dotenv import load_dotenv 


load_dotenv() 


import sys
import os
sys.path.append(os.path.dirname(__file__))
from email_sender import EmailSender

class EmailRequest(BaseModel):
    """
    Модель для простой отправки email
    """
    to_emails: Union[str, List[EmailStr]]  # Может быть строкой или списком
    subject: str                           # Тема письма
    message: str                           # Текст письма
    cc_emails: Union[str, List[EmailStr], None] = None  # Копия (необязательно)

class TransactionAlertRequest(BaseModel):
    """
    Модель для уведомления о транзакции
    """
    to_emails: Union[str, List[EmailStr]]
    transaction_id: str
    account: str
    amount: float
    ml_probability: float
    triggered_rules: List[str]
    cc_emails: Union[str, List[EmailStr], None] = None



app = FastAPI(
    title="Simple Email Service",      # Название API
    description="Простой сервис отправки email уведомлений",  # Описание
    version="1.0.0"                   # Версия
)

# 🔌 ЗАВИСИМОСТИ - функции, которые выполняются перед обработкой запроса

def get_email_sender():
    """
    🔌 ФАБРИКА ДЛЯ СОЗДАНИЯ EMAIL ОТПРАВИТЕЛЯ
    
    Эта функция:
    1. Читает настройки из переменных окружения
    2. Проверяет, что все обязательные настройки есть
    3. Создает и возвращает объект EmailSender
    """
    # 📥 ЧИТАЕМ НАСТРОЙКИ ИЗ ПЕРЕМЕННЫХ ОКРУЖЕНИЯ
    smtp_server = os.getenv("SMTP_SERVER", "smtp.gmail.com")  # Берем из .env или значение по умолчанию
    smtp_port = int(os.getenv("SMTP_PORT", "587"))
    email_login = os.getenv("EMAIL_LOGIN")
    email_password = os.getenv("EMAIL_PASSWORD")
    
    # 🔍 ПРОВЕРЯЕМ ОБЯЗАТЕЛЬНЫЕ НАСТРОЙКИ
    if not email_login or not email_password:
        raise ValueError("❌ EMAIL_LOGIN and EMAIL_PASSWORD must be set in .env file")
    
    # 🏭 СОЗДАЕМ ОБЪЕКТ ДЛЯ ОТПРАВКИ ПИСЕМ
    email_sender = EmailSender(
        smtp_server=smtp_server,
        smtp_port=smtp_port,
        login=email_login,
        password=email_password
    )
    
    print("✅ Email sender created successfully")
    return email_sender

# 🌐 API ENDPOINTS - точки входа для нашего API

@app.post("/send-email")
async def send_email(
    request: EmailRequest,  # Данные из тела запроса
    email_sender: EmailSender = Depends(get_email_sender)  # Зависимость - наш email отправитель
):
    """
    📤 ENDPOINT ДЛЯ ОТПРАВКИ ПРОСТОГО EMAIL
    
    Принимает JSON с данными письма и отправляет его
    """
    print(f"📨 Received request to send email to: {request.to_emails}")
    
    success = email_sender.send_email(
        to_emails=request.to_emails,
        subject=request.subject,
        message=request.message,
        cc_emails=request.cc_emails
    )
    
    # ✅ ВОЗВРАЩАЕМ РЕЗУЛЬТАТ
    if success:
        return {
            "status": "success", 
            "message": "Email отправлен",
            "to": request.to_emails,
            "cc": request.cc_emails
        }
    else:
        # ❌ ЕСЛИ ОШИБКА - ВОЗВРАЩАЕМ ОШИБКУ
        raise HTTPException(
            status_code=500, 
            detail="Ошибка отправки email"
        )

@app.post("/send-transaction-alert")
async def send_transaction_alert(
    request: TransactionAlertRequest,
    email_sender: EmailSender = Depends(get_email_sender)
):
    """
    🚨 ENDPOINT ДЛЯ УВЕДОМЛЕНИЙ О ТРАНЗАКЦИЯХ
    
    Специальный endpoint для отправки уведомлений о подозрительных операциях
    """
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

@app.get("/")
async def root():
    """
    🏠 КОРНЕВОЙ ENDPOINT
    
    Просто показывает, что сервер работает
    """
    return {
        "message": "✅ Email Service is running!",
        "docs": "Visit /docs for API documentation",
        "endpoints": {
            "send_email": "POST /send-email",
            "send_transaction_alert": "POST /send-transaction-alert"
        }
    }

@app.get("/health")
async def health_check():
    """
    HEALTH CHECK ENDPOINT
    
    Используется для проверки работоспособности сервиса
    """
    return {"status": "healthy", "service": "simple-email-api"}

# 🎪 ЗАПУСК ПРИЛОЖЕНИЯ (только при прямом запуске файла)
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)