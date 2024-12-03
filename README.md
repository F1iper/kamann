# :dancer: Dance Studio Reservation System - Backend
Welcome to the Dance Studio Reservation System, built with Java 21 and Spring Boot 3. 
This application manages user authentication, class scheduling, karnets (subscriptions), 
and encompasses all business logic for the dance studio. 
It exposes a REST API for frontend and mobile clients.

## :star2: Features
### :shield: Admin
- Manage classes: Create, edit, and delete classes.
- User management: Register, edit, and deactivate instructors and clients.
#### View histories:
- Instructor attendance.
- Client attendance and karnet usage.
- Generate sales reports: Weekly, monthly, and yearly reports.
## :man_dancing: Instructor
- Manage personal schedules.
- Mark client attendance.
- Issue karnets to clients.
- Confirm cash payments.
- Cancel classes with notifications to participants.
## :woman_dancing: Client
- Book and cancel classes.
- Track karnet usage and expiration.
- Search for classes within specific time ranges.
## :gear: System Logic
### :ticket: Karnets
- Types: 1, 4, 8, or 12 entries (valid for 1 month).
- Rules:
-Cannot purchase a new karnet if an active one exists.
-Entries are deducted upon attendance or late cancellation.
- Expiration reminders are sent 3 days before expiry.
### :calendar: Class Booking
- Requirements: An active karnet or pending payment confirmation.
- Notifications: Email alerts for canceled classes.
### :bar_chart: Reporting
- Sales reports: Generate for specified periods (weekly, monthly, yearly).
- Attendance tracking: For both clients and instructors.
### :hammer_and_wrench: Technologies
- Language: Java 21
- Framework: Spring Boot 3
- Database: PostgreSQL
- Authentication: JWT
- Documentation: Swagger
- Migration Tool: Flyway
### :bangbang: Requirements
- Java 21 installed.
- PostgreSQL database instance.
- Maven build tool.
### :inbox_tray: Installation
Clone the repository:

git clone https://github.com/F1iper/kamann.git

Navigate to the backend directory:

cd kamann/backend

Configure the database:

Update the application.properties file with your PostgreSQL credentials:

spring.datasource.url=jdbc:postgresql://localhost:5432/<your_database_name>
spring.datasource.username=<your_database_username>
spring.datasource.password=<your_database_password>
spring.jpa.hibernate.ddl-auto=validate

Run database migrations: (not implemented yet)

./mvnw flyway:migrate
Run the application:

./mvnw spring-boot:run
Access API documentation:

Open **http://localhost:8080/swagger-ui/index.html** in your browser.

### :file_folder: Project Structure
#### :package: Key Packages
pl.kamann.controllers
Handles REST API endpoints.

pl.kamann.services
Contains business logic and service layer.

pl.kamann.repositories
Interfaces for interacting with the PostgreSQL database.

pl.kamann.models
Data models for users, karnets, and classes.

pl.kamann.config
Configuration for security, database, and global exception handling.

## :memo: To-Do
### :construction: Core Implementation
- Add request/response DTOs.
- Implement mapping between entities and DTOs.
- Finalize karnet management logic.
- Add global exception handling.
### :test_tube: Testing
- Write unit tests for services.
- Create integration tests for API endpoints.
- Add test cases for edge scenarios (e.g., invalid karnet operations).
### :sparkles: Features
- Implement Check-in functionality for instructors.
- Integrate SMTP for email notifications (e.g., reminders, cancellations).
### :mailbox_with_mail: Contact Information
For questions or feedback, please reach out to:

GitHub: https://github.com/F1iper
Email: coderaligator@gmail.com
### :page_facing_up: License
This project is licensed under the MIT License.

In case of any questions or suggestions, please contact me at coderaligator@gmail.com
