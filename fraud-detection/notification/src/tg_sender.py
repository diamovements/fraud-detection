import requests
from typing import List, Union
import os

class TelegramBot:
    def __init__(
        self, 
        bot_token: str = None,
        default_user_ids: Union[str, List[str], None] = None
    ):
        self.bot_token = bot_token or os.getenv('TELEGRAM_BOT_TOKEN')
        self.base_url = f"https://api.telegram.org/bot{self.bot_token}/"
        
        if default_user_ids is None:
            self.default_user_ids = []
        elif isinstance(default_user_ids, str):
            self.default_user_ids = [default_user_ids]
        else:
            self.default_user_ids = default_user_ids
    
    def send_message(
        self,
        message: str,
        user_ids: Union[str, List[str], None] = None,
        parse_mode: str = "HTML",
        disable_notification: bool = False
    ) -> bool:
        """
        Отправляет сообщение пользователям в Telegram
        """
        if user_ids is None:
            user_ids = self.default_user_ids
        elif isinstance(user_ids, str):
            user_ids = [user_ids]
        
        if not user_ids:
            print("Нет user_ids для отправки сообщения")
            return False

        success_count = 0
        total_users = len(user_ids)
        
        print(f"Отправка сообщения {total_users} пользователям: {user_ids}")
        
        for user_id in user_ids:
            try:
                params = {
                    'chat_id': user_id,
                    'text': message,
                    'parse_mode': parse_mode,
                    'disable_notification': disable_notification
                }
                
                response = requests.post(
                    f"{self.base_url}sendMessage",
                    json=params,
                    timeout=10
                )
                
                if response.status_code == 200:
                    success_count += 1
                    print(f"Сообщение отправлено пользователю {user_id}")
                else:
                    error_data = response.json()
                    error_msg = error_data.get('description', 'Unknown error')
                    print(f"Ошибка отправки пользователю {user_id}: {error_msg}")
                    
            except Exception as e:
                print(f"Ошибка при отправке пользователю {user_id}: {e}")
        
        print(f"Итог: отправлено {success_count}/{total_users} сообщений")
        return success_count > 0
            
    def send_transaction_alert(
        self,
        transaction_id: str = "",
        account: str = "",
        amount: float = 0,
        ml_probability: float = 0,
        triggered_rules: List[str] = None,
        user_ids: Union[str, List[str], None] = None,
        priority: str = "medium"
    ) -> bool:
        if triggered_rules is None:
            triggered_rules = []
        
        print(f"Подготовка Telegram уведомления:")
        print(f"   user_ids: {user_ids}")
        print(f"   transaction_id: {transaction_id}")
        print(f"   account: {account}")

        message = f"""
<b>Подозрительная операция</b>

<b>ID транзакции:</b> {transaction_id}
<b>Счет:</b> {account}
<b>Сумма:</b> {amount}
<b>Вероятность мошенничества:</b> {ml_probability * 100}%

<b>Сработавшие правила:</b>
{chr(10).join(f'• {rule}' for rule in triggered_rules)}

<i>Рекомендуется проверить операцию</i>

--
Автоматическая система уведомлений
"""
        
        disable_notification = (priority == "low")
        
        return self.send_message(
            message=message.strip(),
            user_ids=user_ids,
            parse_mode="HTML",
            disable_notification=disable_notification
        )
    
    def test_connection(self) -> bool:
        """
        Проверяет соединение с Telegram API и всё
        """
        try:
            response = requests.get(f"{self.base_url}getMe", timeout=5)
            if response.status_code == 200:
                bot_info = response.json()['result']
                print(f"Бот @{bot_info['username']} подключен успешно")
                return True
            else:
                print(f"Ошибка подключения: {response.json().get('description', 'Unknown error')}")
                return False
        except Exception as e:
            print(f"Ошибка подключения к Telegram: {e}")
            return False