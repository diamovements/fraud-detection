import pandas as pd 
import joblib
import numpy as np
import os

class PredictionModel:
    def __init__(self, models_dir: str):
        self.models_dir = models_dir
        self.models = {}
        print(f"Менеджер моделей инициализирован. Папка: {models_dir}")

    def load_model(self, model_name: str):
        try:
            if model_name in self.models:
                return self.models[model_name]
            

            if not model_name.endswith('.pkl'):
                model_name = model_name + '.pkl'
            
            model_path = os.path.join(self.models_dir, model_name)
            print(f"Пытаемся загрузить модель: {model_path}")
            
            if not os.path.exists(model_path):
                available_models = os.listdir(self.models_dir) if os.path.exists(self.models_dir) else []
                raise FileNotFoundError(f"Файл {model_path} не найден. Доступные модели: {available_models}")
            
            model = joblib.load(model_path)
            self.models[model_name] = model
            print(f"Модель {model_name} успешно загружена")
            return model
            
        except Exception as e:
            print(f'Ошибка загрузки модели {model_name}: {e}')
            raise e

    def predict_proba(self, model_name: str, input_data: dict):
        try:
            model = self.load_model(model_name)
            input_df = pd.DataFrame([input_data])

            numeric_columns = ['amount'] 
            for col in numeric_columns:
                if col in input_df.columns:
                    input_df[col] = input_df[col].astype(float)
                    
            
            probabilities = model.predict_proba(input_df)
            class_1_probability = probabilities[0][1]
            
            print(f"Предсказание завершено. Вероятность: {class_1_probability}")
            return float(class_1_probability)
            
        except Exception as e:
            print(f'Ошибка предсказания: {e}')
            raise e