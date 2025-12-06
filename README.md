# 💳 Payment Microservice

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Stripe](https://img.shields.io/badge/Stripe-008CDD?style=for-the-badge&logo=stripe&logoColor=white)
![Resilience4j](https://img.shields.io/badge/Resilience4j-Circuit_Breaker-red?style=for-the-badge)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)

This repository contains the **Payment Service** for the E-commerce ecosystem. It is a robust financial transaction handler designed with the **Strategy Pattern** to support multiple payment methods (Credit Card via Stripe, Pix) and utilizes **Circuit Breakers** to ensure high availability when communicating with external gateways.

## 🚀 Key Features

* **Multi-Gateway Support**:
    * **Stripe Integration**: Secure credit card processing using the Stripe API.
    * **Pix Support**: Native implementation structure for Brazilian instant payments.
    * **Strategy Pattern**: Easily extensible architecture to add new payment methods (PayPal, Crypto, etc.) without modifying core logic.
* **Fault Tolerance**:
    * Implemented **Resilience4j Circuit Breaker** and **TimeLimiter** to handle Stripe API failures or latency gracefully.
    * Fallback mechanisms defined in `StripeClientFallbackFactory`.
* **Asynchronous Processing**:
    * Consumes payment requests via **RabbitMQ** (`PaymentConsumer`).
    * Publishes `PaymentConcludedEvent` upon successful transactions.
* **Data Integrity**:
    * Transactional consistency using PostgreSQL.
    * Database migrations managed by **Flyway**.

## 🏗️ Architecture & Design Patterns

This service uses a **Factory + Strategy** pattern to select the correct payment processor at runtime:

```java
// Simplified Logic
PaymentStrategy strategy = paymentFactory.getStrategy(paymentType); // Returns CreditCardStrategy or PixStrategy
strategy.process(paymentRequest);
```

### Directory Structure
```text
src/main/java/com/example/payment/
├── config/             # Resilience4j & RabbitMQ Configs
├── consumer/           # RabbitMQ Listeners
├── integration/stripe/ # Feign Clients & Fallbacks for Stripe
├── factory/            # PaymentFactory (Strategy selection)
├── strategy/           # PaymentStrategy Interface & Implementations
└── service/            # Business Logic
```

## 🛠️ Tech Stack

* **Language**: Java 21
* **Framework**: Spring Boot 4.0.0
* **Cloud**: Spring Cloud 2025.1.0 (OpenFeign)
* **Database**: PostgreSQL
* **Resilience**: Resilience4j (Circuit Breaker)
* **Messaging**: RabbitMQ
* **Build Tool**: Gradle 9.0

## ⚙️ Environment Configuration

Create a `.env` file in the root directory based on `.env.example`:

```env
# Database
POSTGRES_HOST=db
POSTGRES_DB=payment_db
POSTGRES_PORT=5432
POSTGRES_USER=postgres
POSTGRES_PASSWORD=secret

# RabbitMQ
RABBITMQ_DEFAULT_USER=guest
RABBITMQ_DEFAULT_PASS=guest
SPRING_RABBITMQ_HOST=rabbitmq
SPRING_RABBITMQ_PORT=5672
RABBITMQ_QUEUE_PAYMENT=payment_queue

# External Gateways
STRIPE_SECRET_KEY=sk_test_your_stripe_key_here
```

## 🐳 Running with Docker

The service is integrated into the shared e-commerce network.

1.  **Build and Start:**
    ```bash
    docker-compose up -d --build
    ```

2.  **Verify Health:**
    The service exposes Actuator endpoints for health and metrics.
    * **Health Check**: `http://localhost:8082/payment/api/actuator/health`
    * **Prometheus**: `http://localhost:8082/payment/api/actuator/prometheus`

3.  **API Documentation**:
    Access the Swagger UI at:
    `http://localhost:8082/payment/api/docs/swagger-ui/index.html`

## 📦 Manual Installation

1.  **Install Dependencies:**
    ```bash
    ./gradlew clean build -x test
    ```

2.  **Run Application:**
    ```bash
    ./gradlew bootRun
    ```

## 🛡️ Resilience Configuration

The service is configured to handle external failures:
* **Sliding Window**: 10 calls.
* **Failure Threshold**: 50% (Open circuit if 5/10 fail).
* **Wait Duration**: 5 seconds in open state before retrying.
* **Timeout**: 5 seconds per call to Stripe.

## 🤝 Contributing

1.  Fork the repository.
2.  Create a feature branch.
3.  Implement a new `PaymentStrategy` if adding a new method.
4.  Submit a Pull Request.

## 📄 License

Distributed under the MIT License.
