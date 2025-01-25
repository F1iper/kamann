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

### 3. PostgreSQL 16 Database Setup

#### Option 1: Docker Installation (Recommended)
```bash
# Pull PostgreSQL 16 image
docker pull postgres:16

# Run PostgreSQL container
docker run --name kamann-postgres - e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16

# Optional: Connect to the database
docker exec -it kamann-postgres psql -U postgres
```

#### Option 2: Local Installation

##### For Ubuntu/Debian:
```bash
# Add PostgreSQL repository
sudo sh -c 'echo "deb https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -

# Install PostgreSQL 16
sudo apt update
sudo apt install postgresql-16

# Start PostgreSQL service
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Create database
sudo -u postgres psql
postgres=# CREATE DATABASE kamann;
postgres=# \q
```

##### For MacOS (using homebrew):
```bash
# Install PostgreSQL 16
brew install postgresql@16

# Start PostgreSQL service
brew services start postgresql@16

# Create database
psql postgres
postgres=# CREATE DATABASE kamann;
postgres=# \q
```

##### For Windows:
1. Download installer from PostgreSQL Downloads
2. Run the installer
3. Use pgAdmin or psql to create the database
   
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

####  Registration Endpoint:
  **POST /api/auth/register**
  
  Request Body (using RegisterRequest):
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "CLIENT"
}
```
  
####  Authorization: 
  Use JWT token from
  **POST /api/auth/login**
  
  Request Body:
  ```json
  {
  "email": "user@example.com",
  "password": "password123"
  }
```


### 🚦 CI/CD Pipeline
  Automated builds and tests via GitHub Actions
  
  Integration testing with Testcontainers
  
  Production secrets managed through GitHub Secrets
  

### 🖥️ Frontend
  Client available at:
**[https://github.com/Osinek280/kamann](https://github.com/Osinek280/kamann)** (in progress)

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
