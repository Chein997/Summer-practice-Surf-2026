# Проверка Apex API через curl

Все команды запускать, когда сервер поднят:

```bash
cd backend
DATABASE_URL="postgres://apex:apex@127.0.0.1:5434/apex?sslmode=disable" make run
```

## 1. Health / Ready

```bash
curl http://127.0.0.1:8080/healthz
curl http://127.0.0.1:8080/readyz
```

Ожидаемо:

```json
{"status":"ok"}
{"status":"ready"}
```

## 2. Запрос SMS

```bash
curl -X POST http://127.0.0.1:8080/auth/request-sms \
  -H "Content-Type: application/json" \
  -d '{"phone":"+79990000001"}'
```

Ожидаемо:

```json
{"status":"sent"}
```

## 3. Проверка SMS и получение token

```bash
curl -X POST http://127.0.0.1:8080/auth/verify-sms \
  -H "Content-Type: application/json" \
  -d '{"phone":"+79990000001","code":"1111"}'
```

Сохрани `accessToken` из ответа:

```bash
TOKEN="<accessToken>"
```

Автоматически через `python3`:

```bash
TOKEN=$(curl -s -X POST http://127.0.0.1:8080/auth/verify-sms \
  -H "Content-Type: application/json" \
  -d '{"phone":"+79990000001","code":"1111"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["accessToken"])')
echo "$TOKEN"
```

## 4. Список слотов

```bash
curl "http://127.0.0.1:8080/ride-slots?days=7&includeUnavailable=true"
```

## 5. Детали слота

```bash
curl http://127.0.0.1:8080/ride-slots/50000000-0000-0000-0000-000000000001
```

## 6. Создание брони

```bash
curl -X POST http://127.0.0.1:8080/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "slotId":"50000000-0000-0000-0000-000000000002",
    "profile":{
      "fullName":"Тестовый Клиент",
      "phone":"+79990000001",
      "email":"test@example.com",
      "age":24
    },
    "safetyRulesAccepted":true,
    "parentalConsentAccepted":false
  }'
```

## 7. Мои брони

```bash
curl -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/bookings
```

## 8. Детали брони

Подставь `id` брони из ответа:

```bash
BOOKING_ID="<bookingId>"
curl -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/bookings/$BOOKING_ID
```

## 9. Отмена брони

```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  http://127.0.0.1:8080/bookings/$BOOKING_ID/cancel
```

## 10. Регистрация push-token

```bash
curl -X POST http://127.0.0.1:8080/push/device-tokens \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"platform":"ANDROID","token":"dev-token-from-curl","appVersion":"1.0.0","locale":"ru-RU"}'
```

## 11. Удаление push-token

```bash
curl -X DELETE "http://127.0.0.1:8080/push/device-tokens?token=dev-token-from-curl" \
  -H "Authorization: Bearer $TOKEN"
```
