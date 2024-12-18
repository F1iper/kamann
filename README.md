# :dancer: Dance Studio Reservation System - Backend

Welcome to the Dance Studio Reservation System, built with **Java 21** and **Spring Boot 3**.  
This application manages user authentication, class scheduling, membership cards (subscriptions),  
and encompasses all business logic for the dance studio.  
It exposes a REST API for frontend and mobile clients.

## :star2: Features

### :shield: Admin
- **Class Management**: Create, edit, and delete classes.
- **User Management**: Register, edit, and deactivate instructors and clients.
- **Histories Overview**:
  - View instructor attendance history.
  - View client attendance and membership card usage history.
- **Reporting**:
  - Generate sales reports: Weekly, monthly, and yearly.

### :man_dancing: Instructor
- **Schedule Management**: Manage personal schedules efficiently.
- **Attendance Management**: Mark attendance for clients.
- **Membership Card Issuance**: Issue membership cards to clients.
- **Payment Handling**: Confirm cash payments.
- **Class Cancellations**: Cancel classes and notify participants.

### :woman_dancing: Client
- **Class Booking**: Book and cancel classes with ease.
- **Membership Card Tracking**: Monitor membership card usage and expiration.
- **Class Search**: Find classes within specific date and time ranges.

## :gear: System Logic

### :ticket: Membership Cards
- **Types**:
  - 1, 4, 8, or 12 entries, valid for 1 month.
- **Rules**:
  - Cannot purchase a new membership card if an active one exists.
  - Entries are deducted upon attendance or late cancellation.
  - Expiration reminders sent 3 days before expiry.

### :calendar: Class Booking
- **Requirements**:
  - An active membership card or pending payment confirmation is required.
- **Notifications**:
  - Email alerts sent for canceled classes.

### :bar_chart: Reporting
- **Sales Reports**: Generate reports for specified periods (weekly, monthly, yearly).
- **Attendance Reports**:
  - Track attendance patterns for both clients and instructors to enable informed decisions.

## :hammer_and_wrench: Technologies

- **Language**: Java 21
- **Framework**: Spring Boot 3
- **Database**: PostgreSQL
- **Authentication**: JWT (JSON Web Token)
- **Documentation**: Swagger (OpenAPI 3.0)
- **Migration Tool**: Flyway
- **Message Queue**: Embedded RabbitMQ using Testcontainers (for testing and development)

## :bangbang: Requirements

- **Java 21** installed.
- **Maven** build tool.
- A **.env** file with the necessary configuration.

## :inbox_tray: Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/F1iper/kamann.git
   
2. Copy the **.env-example** to **.env**:
    ```bash
   cd kamann/backend
   cp .env-example .env

3. Fill in your environment variables in the **.env** file.
 - Update your db credentials and that's it.

    Example **.env**:
   ```bash
   # POSTGRESQL CONFIGURATION
    SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/<your_database_name>
    POSTGRES_USER=<your_database_username>
    POSTGRES_PASSWORD=<your_database_password>

    # SERVER CONFIGURATION
    SERVER_PORT=8080

    # JWT CONFIGURATION
    JWT_SECRET=daf66e01593f61a15b857cf433aae03a005812b31234e149036bcc8dee755dbb
    JWT_EXPIRATION_TIME=86400000

    # RABBITMQ CONFIGURATION (embedded via Testcontainers - no manual setup needed)
    SPRING_RABBITMQ_HOST=localhost
    SPRING_RABBITMQ_PORT=5672
    RABBITMQ_USERNAME=guest
    RABBITMQ_PASSWORD=guest

    # RABBITMQ QUEUES
    MEMBERSHIPCARD_HISTORY_EXCHANGE=history-exchange
    MEMBERSHIPCARD_HISTORY_QUEUE=history-queue
    MEMBERSHIPCARD_HISTORY_ROUTINGKEY=history-routing-key

4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run

5. **Access API documentation**:
Open **http://localhost:8080/swagger-ui/index.html** in your browser.

## :rocket: CI/CD

### The CI/CD pipeline is configured with GitHub Actions:

#### - Builds the application.
#### - Runs integration tests using Testcontainers.
#### - Secrets are securely managed via GitHub Secrets.

## Key Points:
#### - Developers do not need RabbitMQ installed locally.
#### - Testcontainers handles RabbitMQ automatically.
#### - The user must provide a **.env** file for local development.
#### - For production, configure RabbitMQ manually in application.properties.

## :snowflake: Front-end (React)
### Is here:
  https://github.com/remkro/kamann-fe

### Remaining To-Do List

#### **Core Features**
1. **Membership Card Management**
   - Complete logic for:
     - Purchasing membership cards.
     - Validating existing active cards.
     - Deducting entries for attendance or late cancellations.
   - Send expiration reminders.
2. **Class Scheduling**
   - Extend admin functionalities to manage instructor assignments dynamically.
   - Add validation for overlapping schedules.
3. **Reporting**
   - Implement APIs for generating sales reports (weekly, monthly, yearly).
   - Generate exportable reports (CSV/PDF) for attendance and revenue.

#### **System Logic**
4. **Notification System**
   - Integrate SMTP for email notifications:
     - Class cancellations.
     - Membership card expiration reminders.
5. **Advanced Analytics**
   - Attendance patterns for client retention.
   - Instructor performance insights.

#### **Testing**
6. **API Integration Tests**
   - Validate REST endpoints for all services.
   - Test authentication and role-based access.
7. **Edge Case Handling**
   - Ensure robust error handling for invalid inputs and missing entities.

#### **Documentation**
8. **Swagger Integration**
   - Finalize Swagger documentation for all endpoints.
   - Add examples for request/response bodies.

#### **Deployment**
9. **CI/CD**
   - Configure GitHub Actions or Jenkins for automated testing and deployment.
10. **Production Readiness**
    - Optimize database indexes and query performance.
    - Add application monitoring tools (e.g., Prometheus, Grafana).

## :page_facing_up: License
This project is licensed under the MIT License.
