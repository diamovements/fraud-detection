from fastapi import FastAPI, HTTPException
import numpy as np
import os
from .models.model import PredictionModel

current_dir = os.path.dirname(os.path.abspath(__file__))
model_dir = os.path.join(current_dir, 'data') 

app = FastAPI(title="Fraud Detection API")


prediction_model = PredictionModel(models_dir=model_dir)

@app.get('/')
def read_root():
    return {'message': 'Алгоритм предсказания вероятности быть фрод классом'}

@app.get('/health')
def health_check():
    return {'status': 'healthy', 'message': 'API работает корректно'}

@app.post('/predict')
async def predict(model_name: str, data: dict):  
    try:
        prediction = prediction_model.predict_proba(model_name, data)
        return {
            'model_used': model_name,
            'fraud_probability': prediction,  
            'status': 'success'
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Ошибка предсказания: {str(e)}")


@app.get('/models/{model_name}/check')
async  def check_model(model_name: str):
    '''
    Проверка существования и загрузки модели
    '''
    try:
        model = prediction_model.load_model(model_name)
        return {
            'model_name': model_name,
            'status': 'available',
            'model_type': type(model).__name__
        }
    except Exception as e:
        raise HTTPException(status_code=404, detail=f"Модель не найдена: {str(e)}")