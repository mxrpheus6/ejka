import os
import json
import requests
import time
import pika
from minio import Minio
import cv2
import numpy as np
import pytesseract
import re
from fuzzywuzzy import process, fuzz
from typing import List, Tuple

APP_ENV = os.getenv("APP_ENV", "prod").lower()

if APP_ENV == "prod":
    RABBIT_HOST = os.getenv("RABBIT_HOST", "rabbitmq")
    MINIO_ENDPOINT = os.getenv("MINIO_ENDPOINT", "minio:9000")
    APP_API_URL = os.getenv("APP_API_URL", "http://app:7000/api/v1/additives/worker-view")
    APP_API_ALLERGENS_URL = os.getenv("APP_API_ALLERGENS_URL", "http://app:7000/api/v1/allergen-triggers")
    TESSERACT_CMD = os.getenv("TESSERACT_CMD", "/usr/bin/tesseract")
else:
    RABBIT_HOST = os.getenv("RABBIT_HOST", "localhost")
    MINIO_ENDPOINT = os.getenv("MINIO_ENDPOINT", "localhost:9000")
    APP_API_URL = os.getenv("APP_API_URL", "http://localhost:7000/api/v1/additives/worker-view")
    APP_API_ALLERGENS_URL = os.getenv("APP_API_ALLERGENS_URL", "http://localhost:7000/api/v1/allergen-triggers")
    TESSERACT_CMD = os.getenv("TESSERACT_CMD", r'C:\Program Files\Tesseract-OCR\tesseract.exe')

pytesseract.pytesseract.tesseract_cmd = TESSERACT_CMD

RABBIT_PORT = int(os.getenv("RABBIT_PORT", 5672))
RABBIT_USER = os.getenv("RABBIT_USER", "admin")
RABBIT_PASS = os.getenv("RABBIT_PASS", "admin123")

MINIO_USER = os.getenv("MINIO_USER", "admin")
MINIO_PASS = os.getenv("MINIO_PASS", "qwerty192")
MINIO_BUCKET = os.getenv("MINIO_BUCKET", "ejka-products")

EXCHANGE_NAME = "image.exchange"
QUEUE_REQUESTS = "q.image.processing.requests"
QUEUE_RESPONSES = "q.image.processing.responses"
RK_RESPONSE = "image.response.rk"

QUEUE_ADDITIVE_UPDATES = os.getenv("RABBIT_QUEUE_ADDITIVE_UPDATES", "q.additive.updates")
RK_ADDITIVE_UPDATE = "additive.update.rk"

minio_client = Minio(
    MINIO_ENDPOINT,
    access_key=MINIO_USER,
    secret_key=MINIO_PASS,
    secure=False
)

CONTROVERSIAL_DB = {
    "Пальмовое масло": ["пальмов", "пальмоядров"],
}

def load_etalons_from_api(api_url: str, max_retries=10, delay=5) -> Tuple[dict, dict]:
    print(f"[*] Ожидание запуска монолита и загрузка данных из {api_url}...")
    for attempt in range(max_retries):
        try:
            response = requests.get(api_url, timeout=10)
            response.raise_for_status()

            data = response.json()
            codes_mapping = {}
            names_mapping = {}

            for item in data:
                additive_id = item.get("id")
                code = item.get("code")
                name = item.get("nameRu")

                additive_obj = {"id": additive_id, "code": code, "nameRu": name}

                if code:
                    codes_mapping[code.lower().strip()] = additive_obj
                if name:
                    names_mapping[name.lower().strip()] = additive_obj

            print(f"[ИНФО] Успех! Добавлено {len(codes_mapping)} кодов и {len(names_mapping)} текстовых названий.")
            return codes_mapping, names_mapping

        except requests.exceptions.RequestException as e:
            print(f"[ВНИМАНИЕ] Попытка {attempt + 1}/{max_retries}. API недоступно: {e}")
            time.sleep(delay)

    print("[ОШИБКА] Не удалось связаться с монолитом! Воркер будет работать вхолостую.")
    return {}, {}

def load_allergens_from_api(api_url: str, max_retries=10, delay=5) -> dict:
    print(f"[*] Ожидание запуска монолита и загрузка аллергенов из {api_url}...")
    for attempt in range(max_retries):
        try:
            response = requests.get(api_url, timeout=10)
            response.raise_for_status()

            data = response.json()
            allergens_db = {}

            for item in data:
                category = item.get("category")
                trigger = item.get("triggerWord")

                if category and trigger:
                    if category not in allergens_db:
                        allergens_db[category] = []
                    allergens_db[category].append(trigger.lower().strip())

            print(f"[ИНФО] Успех! Загружено {len(allergens_db)} категорий аллергенов.")
            return allergens_db

        except requests.exceptions.RequestException as e:
            print(f"[ВНИМАНИЕ] Попытка {attempt + 1}/{max_retries}. API аллергенов недоступно: {e}")
            time.sleep(delay)

    print("[ОШИБКА] Не удалось загрузить аллергены! Справочник будет пустым.")
    return {}

CODES_MAPPING, NAMES_MAPPING = load_etalons_from_api(APP_API_URL)
ALLERGENS_DB = load_allergens_from_api(APP_API_ALLERGENS_URL)


def apply_threshold(img, argument):
    if argument == "adaptive":
        blurred = cv2.GaussianBlur(img, (5, 5), 0)
        return cv2.adaptiveThreshold(
            blurred, 255,
            cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
            cv2.THRESH_BINARY,
            31,
            10
        )

    switcher = {
        "GaussianBlur": cv2.threshold(cv2.GaussianBlur(img, (5, 5), 0), 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)[1],
        "bilateralFilter":
            cv2.threshold(cv2.bilateralFilter(img, 5, 75, 75), 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)[1],
        "medianBlur": cv2.threshold(cv2.medianBlur(img, 3), 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)[1],
    }
    return switcher.get(argument, img)


def normalize_text(text: str) -> str:
    text = text.lower()
    text = re.sub(r'[«»"\'\`\.]', '', text)
    text = re.sub(r'\s+', ' ', text).strip()
    return text

def fix_ecode_ocr_typos(text: str) -> str:
    text = re.sub(r'([eе]\s*\d{3})4', r'\g<1>a', text, flags=re.IGNORECASE)
    return text

def smart_text_scorer(s1, s2, **kwargs):
    r1 = fuzz.ratio(s1, s2)
    r2 = fuzz.token_sort_ratio(s1, s2)
    return max(r1, r2)


def parse_and_fuzzy_match(ocr_text: str, codes_map: dict, names_map: dict, cutoff: int = 80) -> List[dict]:
    print("[ШАГ 3] Применяем жадный поиск (Greedy Forward Scan)...")

    ocr_text = re.sub(r'[Ff£€эЭёЁ]-?(?=[0-9OoОоЗз]{3,4})', 'e', ocr_text)

    def fix_digits(match):
        val = match.group(0)
        val = re.sub(r'[OoОо]', '0', val)
        val = re.sub(r'[Зз]', '3', val)
        val = re.sub(r'[бБ]', '6', val)
        return val

    ocr_text = re.sub(r'[eеEЕ]-?[0-9OoОоЗзбБ]{3,4}', fix_digits, ocr_text)

    ocr_text = re.sub(r'([eеEЕ]\d{3})4', r'\g<1>a', ocr_text, flags=re.IGNORECASE)

    print(ocr_text)

    clean_ingredients = []
    seen_additive_ids = set()
    code_keys = list(codes_map.keys())
    name_keys = list(names_map.keys())

    found_e_codes = re.findall(r'[eе]-?\d{3,4}[a-zа-я]?', ocr_text, flags=re.IGNORECASE)

    found_e_codes = [
        code.lower().replace(' ', '').replace('-', '').replace('е', 'e')
        for code in found_e_codes
    ]

    for raw_code in found_e_codes:
        best_code = process.extractOne(raw_code.lower().strip(), code_keys, scorer=fuzz.ratio)
        if best_code and best_code[1] >= 75:
            matched_key, score = best_code
            additive_info = codes_map[matched_key]
            additive_id = additive_info.get("id")

            if additive_id not in seen_additive_ids:
                clean_ingredients.append({
                    "raw": raw_code,
                    "id": additive_id,
                    "code": additive_info.get("code"),
                    "nameRu": additive_info.get("nameRu"),
                    "score": score
                })
                seen_additive_ids.add(additive_id)
                print(f"  [+ КОД] Извлечено: '{raw_code}' -> {additive_info.get('code')} ({score}%)")

        ocr_text = ocr_text.replace(raw_code, ' ')

    text = ocr_text.lower()
    text = re.sub(r'[^а-яёa-z0-9\s]', ' ', text)

    words = [w for w in text.split() if len(w) > 1 or w in ['и', 'с']]

    MAX_NGRAM_SIZE = 4

    i = 0
    while i < len(words):
        best_overall_match = None
        best_overall_score = 0
        best_ngram_len = 0
        best_ngram_phrase = ""

        for n in range(1, MAX_NGRAM_SIZE + 1):
            if i + n > len(words):
                break

            ngram_phrase = " ".join(words[i:i + n])

            if len(ngram_phrase) < 3:
                continue

            best_name = process.extractOne(ngram_phrase, name_keys, scorer=smart_text_scorer)

            if best_name:
                matched_key, score = best_name

                current_cutoff = cutoff + 8 if n == 1 else cutoff

                if score >= current_cutoff and score > best_overall_score:
                    best_overall_score = score
                    best_overall_match = names_map[matched_key]
                    best_ngram_len = n
                    best_ngram_phrase = ngram_phrase

        if best_overall_match:
            additive_id = best_overall_match.get("id")

            if additive_id not in seen_additive_ids:
                clean_ingredients.append({
                    "raw": best_ngram_phrase,
                    "id": additive_id,
                    "code": best_overall_match.get("code"),
                    "nameRu": best_overall_match.get("nameRu"),
                    "score": best_overall_score
                })
                seen_additive_ids.add(additive_id)
                print(
                    f"  [+ ТЕКСТ] Найдено: '{best_ngram_phrase}' -> {best_overall_match.get('nameRu')} ({best_overall_score}%)")

            i += best_ngram_len
        else:
            i += 1

    return clean_ingredients


def parse_allergens(ocr_text: str, cutoff: int = 80) -> List[dict]:
    print("[ШАГ 4] Поиск аллергенов по ТР ТС...")
    text = ocr_text.lower()
    text = re.sub(r'[^а-яёa-z0-9\s]', ' ', text)
    words = [w for w in text.split() if len(w) > 2]

    found_allergens = []
    seen_categories = set()

    i = 0
    while i < len(words):
        best_overall_category = None
        best_overall_score = 0
        best_matched_phrase = ""
        best_ngram_len = 1

        for n in [1, 2]:
            if i + n > len(words):
                break

            ngram_phrase = " ".join(words[i:i + n])

            for category, triggers in ALLERGENS_DB.items():
                if category in seen_categories:
                    continue

                best_match = process.extractOne(ngram_phrase, triggers, scorer=fuzz.ratio)

                if best_match:
                    score = best_match[1]
                    if score >= cutoff and score > best_overall_score:
                        best_overall_score = score
                        best_overall_category = category
                        best_matched_phrase = ngram_phrase
                        best_ngram_len = n

        if best_overall_category:
            found_allergens.append({
                "category": best_overall_category,
                "matchedText": best_matched_phrase,
                "score": best_overall_score
            })
            seen_categories.add(best_overall_category)
            print(f"  [+ АЛЛЕРГЕН] Найдено: '{best_matched_phrase}' -> {best_overall_category} ({best_overall_score}%)")
            i += best_ngram_len
        else:
            i += 1

    return found_allergens


def parse_controversial(ocr_text: str, cutoff: int = 85) -> List[dict]:
    print("[ШАГ 5] Поиск спорных ингредиентов...")
    text = ocr_text.lower()
    text = re.sub(r'[^а-яёa-z0-9\s]', ' ', text)
    words = [w for w in text.split() if len(w) > 2]

    found_controversial = []
    seen_categories = set()

    i = 0
    while i < len(words):
        best_overall_category = None
        best_overall_score = 0
        best_matched_phrase = ""
        best_ngram_len = 1

        for n in range(1, 4):
            if i + n > len(words):
                break

            ngram_phrase = " ".join(words[i:i + n])

            for category, triggers in CONTROVERSIAL_DB.items():
                if category in seen_categories:
                    continue

                best_match = process.extractOne(ngram_phrase, triggers, scorer=fuzz.ratio)

                if best_match:
                    score = best_match[1]
                    if score >= cutoff and score > best_overall_score:
                        best_overall_score = score
                        best_overall_category = category
                        best_matched_phrase = ngram_phrase
                        best_ngram_len = n

        if best_overall_category:
            found_controversial.append({
                "category": best_overall_category,
                "matchedText": best_matched_phrase,
                "score": best_overall_score
            })
            seen_categories.add(best_overall_category)
            print(f"  [+ СПОРНОЕ] Найдено: '{best_matched_phrase}' -> {best_overall_category} ({best_overall_score}%)")
            i += best_ngram_len
        else:
            i += 1

    return found_controversial


def process_image_bytes(img_bytes: bytes, method: str) -> str:
    print(f"[ШАГ 2] Декодируем изображение ({len(img_bytes)} байт)...")

    nparr = np.frombuffer(img_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if img is None:
        raise ValueError("OpenCV не смог декодировать изображение.")

    print(f"  -> Изображение успешно декодировано. Размер: {img.shape}")

    img = cv2.resize(img, None, fx=1.5, fy=1.5, interpolation=cv2.INTER_CUBIC)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    img = apply_threshold(img, method)

    print("  -> Запускаем Tesseract OCR...")

    custom_config = r'--oem 3 --psm 6'
    result = pytesseract.image_to_string(img, lang='rus+eng', config=custom_config)

    preview = result.replace('\n', ' ')[:150]
    print(f"  -> OCR завершен. Превью текста: '{preview}...'")

    return result

def process_message(ch, method, properties, body):
    try:
        print("\n" + "=" * 50)
        request_data = json.loads(body.decode('utf-8'))
        id = request_data.get("id")
        object_key = request_data.get("objectKey")

        if not object_key:
            raise ValueError("В сообщении отсутствует поле 'objectKey'")

        print(f"[*] СТАРТ ОБРАБОТКИ: {object_key}")

        print(f"[ШАГ 1] Скачивание {object_key} из бакета {MINIO_BUCKET}...")
        response = minio_client.get_object(MINIO_BUCKET, object_key)
        img_bytes = response.read()
        response.close()
        response.release_conn()

        ocr_text = process_image_bytes(img_bytes, "adaptive")

        additives_list = parse_and_fuzzy_match(ocr_text, CODES_MAPPING, NAMES_MAPPING, cutoff=80)

        allergens_list = parse_allergens(ocr_text, cutoff=85)

        controversial_list = parse_controversial(ocr_text, cutoff=85)

        result_payload = {
            "id": id,
            "objectKey": object_key,
            "status": "SUCCESS",
            "parsedText": ocr_text.strip(),
            "additives": additives_list,
            "allergens": allergens_list,
            "controversial": controversial_list
        }
        print(f"[УСПЕХ] Обработка {object_key} завершена. Найдено ингредиентов: {len(additives_list)}. Найдено аллергенов: {len(allergens_list)}")
        print(f"Аллергены: {allergens_list}")
        print(f"Спорные: {controversial_list}")

    except Exception as e:
        print(f"[ОШИБКА] При обработке {object_key} произошла ошибка: {str(e)}")
        import traceback
        traceback.print_exc()

        result_payload = {
            "objectKey": json.loads(body.decode('utf-8')).get("objectKey", "unknown"),
            "status": "ERROR",
            "errorMessage": str(e)
        }

    ch.basic_publish(
        exchange=EXCHANGE_NAME,
        routing_key=RK_RESPONSE,
        body=json.dumps(result_payload, ensure_ascii=False),
        properties=pika.BasicProperties(
            content_type='application/json',
            correlation_id=properties.correlation_id
        )
    )
    print(f"[*] Результат отправлен в очередь {QUEUE_RESPONSES}")
    print("=" * 50 + "\n")

    ch.basic_ack(delivery_tag=method.delivery_tag)

def process_additive_update(ch, method, properties, body):
    global CODES_MAPPING, NAMES_MAPPING
    print("\n" + "=" * 50)
    print("[*] СИГНАЛ: База добавок изменилась. Выполняю горячую перезагрузку справочников...")

    try:
        new_codes, new_names = load_etalons_from_api(APP_API_URL, max_retries=3, delay=2)

        if new_codes and new_names:
            CODES_MAPPING.clear()
            NAMES_MAPPING.clear()

            CODES_MAPPING = new_codes
            NAMES_MAPPING = new_names

            print(f"[УСПЕХ] Справочники успешно обновлены! В памяти {len(CODES_MAPPING)} кодов.")
        else:
            print("[ВНИМАНИЕ] Не удалось загрузить новые данные. Оставляем старый кэш.")

    except Exception as e:
        print(f"[ОШИБКА] При обновлении кэша произошла ошибка: {str(e)}")

    ch.basic_ack(delivery_tag=method.delivery_tag)
    print("=" * 50 + "\n")

def start_worker():
    credentials = pika.PlainCredentials(RABBIT_USER, RABBIT_PASS)
    parameters = pika.ConnectionParameters(host=RABBIT_HOST, port=RABBIT_PORT, credentials=credentials)

    print(f"[*] Подключение к RabbitMQ на {RABBIT_HOST}:{RABBIT_PORT}...")
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    channel.exchange_declare(exchange=EXCHANGE_NAME, exchange_type='direct', durable=True)

    channel.queue_declare(queue=QUEUE_REQUESTS, durable=True)
    channel.queue_declare(queue=QUEUE_RESPONSES, durable=True)
    channel.queue_declare(queue=QUEUE_ADDITIVE_UPDATES, durable=True)

    channel.queue_bind(exchange=EXCHANGE_NAME, queue=QUEUE_REQUESTS, routing_key="image.request.rk")
    channel.queue_bind(exchange=EXCHANGE_NAME, queue=QUEUE_ADDITIVE_UPDATES, routing_key=RK_ADDITIVE_UPDATE)

    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue=QUEUE_REQUESTS, on_message_callback=process_message)
    channel.basic_consume(queue=QUEUE_ADDITIVE_UPDATES, on_message_callback=process_additive_update)

    print('[*] Сервис запущен. Ожидание сообщений (CTRL+C для выхода)...')
    channel.start_consuming()


if __name__ == '__main__':
    start_worker()