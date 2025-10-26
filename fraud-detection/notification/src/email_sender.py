import smtplib
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from typing import List, Union
import os

class EmailSender:
    def __init__(
        self, 
        smtp_server: str = "smtp.gmail.com",
        smtp_port: int = 587,
        login: str = None,
        password: str = None
    ):
        """
        Простой класс для отправки email
        
        Args:
            smtp_server: SMTP сервер (по умолчанию gmail)
            smtp_port: Порт SMTP (по умолчанию 587)
            login: Ваш email
            password: Пароль приложения
        """
        self.smtp_server = smtp_server
        self.smtp_port = smtp_port
        self.login = login
        self.password = password
    
    def send_email(
        self,
        to_emails: Union[str, List[str]],
        subject: str,
        message: str,
        cc_emails: Union[str, List[str], None] = None
    ) -> bool:
        """
        Отправляет простое email сообщение
        
        Args:
            to_emails: Основные получатели (строка или список)
            subject: Тема письма
            message: Текст сообщения
            cc_emails: Получатели копии (строка или список)
            
        Returns:
            bool: Успешно ли отправлено
        """
        try:
            if isinstance(to_emails, str):
                to_emails = [to_emails]
            
            if isinstance(cc_emails, str):
                cc_emails = [cc_emails]
            elif cc_emails is None:
                cc_emails = []
            
            # Создаем сообщение
            msg = MIMEMultipart()
            msg['From'] = self.login
            msg['To'] = ', '.join(to_emails)
            msg['Subject'] = subject
            
            if cc_emails:
                msg['Cc'] = ', '.join(cc_emails)
            
            msg.attach(MIMEText(message, 'plain'))
            
            all_recipients = to_emails + cc_emails
            
            print(f"📧 Отправка email:")
            print(f"   От: {self.login}")
            print(f"   Кому: {to_emails}")
            if cc_emails:
                print(f"   Копия: {cc_emails}")
            print(f"   Тема: {subject}")
            
            with smtplib.SMTP(self.smtp_server, self.smtp_port) as server:
                server.starttls()  # Включаем шифрование
                server.login(self.login, self.password)
                server.send_message(msg, to_addrs=all_recipients)
            
            print("Email успешно отправлен!")
            return True
            
        except Exception as e:
            print(f"Ошибка отправки email: {e}")
            return False
    
    def send_transaction_alert(
        self,
        to_emails: Union[str, List[str]],
        transaction_id: str,
        account: str,
        amount: float,
        ml_probability: float,
        triggered_rules: List[str],
        cc_emails: Union[str, List[str], None] = None
    ) -> bool:
        """
        Специальный метод для отправки уведомлений о транзакциях
        
        Args:
            to_emails: Основные получатели
            transaction_id: ID транзакции
            account: Номер счета
            amount: Сумма
            ml_probability: Вероятность мошенничества
            triggered_rules: Список сработавших правил
            cc_emails: Получатели копии
        """
        subject = f"🚨 Подозрительная операция #{transaction_id}"
        
        message = f"""
                            Обнаружена подозрительная операция:

                            ID транзакции: {transaction_id}
                            Счет: {account}
                            Сумма: {amount} руб.
                            Вероятность мошенничества: {ml_probability}%

                            Сработавшее правило
                            
                            
                            :
                            {chr(10).join(f'• {rule}' for rule in triggered_rules)}

                            Рекомендуется проверить операцию.

                            --
                            Автоматическая система уведомлений
"""
        
        return self.send_email(
            to_emails=to_emails,
            subject=subject,
            message=message,
            cc_emails=cc_emails
        )