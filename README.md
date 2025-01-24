# 💃 Dance Studio Reservation System - Backend

A modern reservation system built with **Java 21** and **Spring Boot 3**, featuring role-based access control, membership management, and class scheduling.

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql)
![Maven](https://img.shields.io/badge/Maven-C71A36?logo=apachemaven)
![JWT](https://img.shields.io/badge/JWT-000000?logo=jsonwebtokens&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?logo=swagger&logoColor=black)

## 🌟 Features

### 🛡️ Admin
- Full class lifecycle management
- User administration with activation/deactivation
- Financial reporting (weekly/monthly/yearly)
- Attendance analytics dashboard

### 🕺 Instructor
- Personal schedule management
- Real-time attendance tracking
- Membership validation system
- Class cancellation notifications

### 💃 Client
- Class booking system with membership integration
- Membership usage tracking
- Advanced class search filters

## 🛠️ Technologies

- **Core**: Java 21 • Spring Boot 3
- **Database**: PostgreSQL 16
- **Security**: JWT Authentication
- **API Docs**: Swagger/OpenAPI 3.0
- **CI/CD**: GitHub Actions

## 🚀 Installation

### 1. Clone Repository and change into the project directory
  ```bash
  git clone https://github.com/F1iper/kamann.git
  cd kamann/backend
  ```

### 2. Create the .env file out of .env-example
```bash
  mv .env-example .env
```

### 3. Database Setup (PostgreSQL 16)
Run these commands in your database (contanerized or not)
```sql
  CREATE DATABASE kamann;
```

### 4. Configure the database
```properties
# POSTGRESQL CONFIGURATION
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/kamann
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# JWT CONFIGURATION
JWT_SECRET=daf66e01593f61a15b857cf433aae03a005812b31234e149036bcc8dee755dbb
JWT_EXPIRATION_TIME=86400000 #default to 24h
```
### 5. Run Application (with PROD profile)
```bash
  ./mvn spring-boot:run -Dspring-boot.run.profiles=PROD
```

## 📚 API Documentation
  Access interactive Swagger UI at:
  **http://localhost:8080/swagger-ui.html**

  Authorization: Use JWT token from **/api/auth/login** endpoint

### 🚦 CI/CD Pipeline
  Automated builds and tests via GitHub Actions
  
  Integration testing with Testcontainers
  
  Production secrets managed through GitHub Secrets
  

### 🖥️ Frontend
  Client available at:
**[https://github.com/remkro/kamann-fe](https://github.com/Osinek280/kamann)** (in progress)

### 📝 Roadmap

#### Core Improvements

🎫 Membership expiration reminders

🕒 Schedule conflict detection

📊 PDF report generation

#### Infrastructure

📧 SMTP integration for notifications

📈 Prometheus/Grafana monitoring

🔍 Query performance optimization

### 📜 License

This project is licensed under the MIT License.
