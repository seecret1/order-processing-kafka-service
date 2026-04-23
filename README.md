# Order Processing Service

Проект состоит из трёх Spring Boot сервисов:

- `data` - хранение и выдача данных из PostgreSQL
- `order-service` - принимает запрос на создание заказа, сохраняет его через `data` и публикует событие в Kafka
- `notification-service` - потребляет события из Kafka и обрабатывает их

## Как запустить локально

В этом проекте Docker используется только для инфраструктуры. Сами Spring-сервисы я запускаю отдельно из IDE или через Gradle.

### 1. Поднять инфраструктуру

Сначала запусти PostgreSQL, ZooKeeper и Kafka:

```bash
docker compose up -d postgres-orders zookeeper kafka
```

Перед запуском проверь `.env` в корне проекта. В нём должны быть заданы:

```env
HOST=localhost
DB_NAME=orders_db
DB_USERNAME=admin
DB_PASSWORD=admin
DB_URL=jdbc:postgresql://localhost:5433/orders_db
DB_PORT=5433
ZOOKEEPER_PORT=2181
KAFKA_PORT=9092
```

### 2. Запустить сервисы

В отдельных терминалах или из IDE запусти:

```bash
.\gradlew.bat :data:bootRun
.\gradlew.bat :order-service:bootRun
.\gradlew.bat :notification-service:bootRun
```

Если удобнее, можно запускать каждый сервис напрямую из IDE по его `main`-классу.

### 3. Порты сервисов

- `data` - `8083`
- `order-service` - `8081`
- `notification-service` - `8082`

## Примеры запросов

### Создать пользователя

```bash
curl -X POST http://localhost:8083/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "id": "user-1",
    "name": "Ivan Petrov"
  }'
```

### Получить всех пользователей

```bash
curl http://localhost:8083/api/users
```

### Получить пользователя по id

```bash
curl http://localhost:8083/api/users/user-1
```

### Создать заказ через data-service

```bash
curl -X POST http://localhost:8083/api/data \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-1",
    "productCode": "SKU-100",
    "quantity": 2,
    "price": 149.90
  }'
```

### Создать заказ через order-service

Этот запрос дополнительно отправит событие в Kafka.

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-1",
    "productCode": "SKU-100",
    "quantity": 2,
    "price": 149.90
  }'
```

Пример ответа:

```json
{
  "orderId": "2c77d3e8-3f9c-4f3d-8d0d-2d0c3b3f4a1d",
  "userId": "user-1",
  "productCode": "SKU-100",
  "quantity": 2,
  "price": 149.90,
  "timestamp": "2026-04-22T10:00:00Z"
}
```

## Теоретические вопросы

### В чём разница между at-least-once и at-most-once доставкой? Какой подход обычно используется в Spring Kafka по умолчанию?

- `At-most-once` означает, что сообщение будет обработано максимум один раз, но при сбое оно может потеряться.
- `At-least-once` означает, что сообщение будет доставлено и обработано как минимум один раз, но возможны дубликаты.
- В Spring Kafka по умолчанию обычно используется поведение `at-least-once`: offset коммитится после обработки сообщения, поэтому при падениях возможна повторная доставка.

### Что такое consumer group? Как она влияет на распределение партиций между экземплярами консьюмеров?

Consumer group - это набор консьюмеров с одинаковым `group.id`, которые совместно читают один topic. Kafka распределяет партиции между экземплярами внутри группы так, чтобы одна партиция одновременно читалась только одним консьюмером в группе. Если экземпляров больше, чем партиций, лишние консьюмеры будут простаивать.

### Зачем нужен Dead Letter Topic? Как он помогает строить отказоустойчивые системы?

Dead Letter Topic нужен для сообщений, которые не удалось обработать после всех попыток повторной доставки. Такие сообщения не блокируют основной поток обработки, а уходят в отдельный topic для анализа, повторной обработки или ручного разбора.

### Как можно добиться идемпотентной обработки сообщений на консьюмере? (кратко)

Сохранять уникальный идентификатор сообщения и проверять, обрабатывали ли его уже. Обычно это делают через уникальный индекс в БД, таблицу обработанных сообщений или upsert-логику.

### Какие способы задания сериализаторов существуют в Spring Kafka?

- Через настройки `application.yml` или `application.properties`:
  - `spring.kafka.producer.key-serializer`
  - `spring.kafka.producer.value-serializer`
  - `spring.kafka.consumer.key-deserializer`
  - `spring.kafka.consumer.value-deserializer`
- Через Java-конфигурацию в `ProducerFactory` / `ConsumerFactory`.
- Через `JsonSerializer` / `JsonDeserializer` для JSON-сообщений.
- Через `@KafkaListener` и кастомные `KafkaMessageConverter`, если нужен более гибкий разбор payload.

## Проверка

Для локальной проверки я использую:

```bash
.\gradlew.bat :data:test :order-service:test :notification-service:test
```

Для отдельных интеграционных тестов с Kafka уже есть Testcontainers-тесты в `order-service` и `notification-service`.
