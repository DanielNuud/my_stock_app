# 📈 my\_stock\_app

A **financial microservices-based application** for visualizing stock charts, analyzing historical stock data, and tracking news with **Java Spring Boot backend** and **React frontend**, implemented using a traditional synchronous approach for clear request-response handling and simplicity.

This project is the **first part of my diploma work**, where the next stage will be building the same application using a reactive approach, allowing direct comparison between synchronous and reactive microservice architectures in terms of design, performance, and scalability.

**Note**: This project is **still in development and not fully finished**, with active work ongoing to complete functionality, testing, and polishing for production readiness.
## 🚀 Features

* Fetch and store **historical stock data** from Polygon.io with chart display.
* Supports **1 day, 1 week, 1 month, 1 year, 5 years** periods.
* Real-time stock data processing via WebSocket.
* Integrated **news service** for company-related news.
* **Currency service** for currency conversion.
* **API Gateway** for routing and securing microservices.
* **Docker and Docker Compose** for deployment.
* **React frontend** with clean UI.
* **Postman collections** for API testing.

## 🛠️ Technologies

* Java 17, Spring Boot 3.4.5
* Spring Data JPA + PostgreSQL + Redis
* WebFlux with block (synchronous)
* WebSocket
* Docker, Docker Compose
* React + Vite
* Polygon.io API
* JUnit, Mockito
* Lombok
* Prometheus + Grafana

## 📂 Architecture

            ┌────────────────────────────┐
            │        React Frontend      │
            │   (charts, news display)   │
            |       (company info)       |
            └──────────────┬─────────────┘
                           │ HTTP REST
                           ▼
                    ┌───────────────┐
                    │  API Gateway  │
                    └──────┬────────┬────────────────────────┬─────────────┐
                           │        │                        │             │
                           ▼        ▼                        ▼             ▼ 
                   ┌────────────┐ ┌────────────────┐ ┌──────────────┐ ┌──────────────┐
                   │ company-   │ │ historical-    │ │ news-service │ │ stocks-      │
                   │ info-      │ │ analytics-     │ │ (news fetch  │ │ service      │
                   │ service    │ │ service        │ │ & storage)   │ │ (real-time   │
                   │ (company   │ │ (historical    │ │              │ │ stock data)  │
                   │ data)      │ │ stock data)    │ │              │ │              │
                   └────┬───────┘ └──────┬─────────┘ └─────┬────────┘ └──────────────┘
                        │                │                 │
            ┌───────────▼──────────┐ ┌───▼──────────┐ ┌────▼────────┐
            │ PostgreSQL + Redis   │ │ PostgreSQL   │ │ PostgreSQL  │
            │ (company-info)       │ │ (historical) │ │ (news)      │
            └──────────────────────┘ └──────────────┘ └─────────────┘


## ⚙️ Installation

```bash
git clone https://github.com/DanielNuud/my_stock_app.git
cd my_stock_app
```

Run backend with Docker Compose:

```bash
docker-compose up --build
```

Visit:

```
http://localhost:5173
```

## 🧪 Testing

* Backend tested with **JUnit and Mockito**
* Postman collections for API checks

## ✨ Future Plans

* Add notification service
* Add currency service on start page

