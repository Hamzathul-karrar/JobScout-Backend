# JobScout Backend

<div align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=java&logoColor=white" alt="Java 17" />
  <br />
  <a href="https://jobscout-app.netlify.app/" target="_blank">
    <img src="https://img.shields.io/badge/Live_Demo-brightgreen.svg?style=for-the-badge" alt="Live Demo" />
  </a>
  <a href="https://github.com/Hamzathul-karrar/JobScout-Frontend" target="_blank">
    <img src="https://img.shields.io/badge/Frontend_Repo-gray.svg?style=for-the-badge&logo=github" alt="Frontend Repo" />
  </a>
</div>

<br />

The **JobScout Backend** is a robust Spring Boot application that powers the JobScout platform. It provides RESTful APIs for user authentication, job searching (integrated with SerpAPI), and data management, ensuring a seamless experience for users looking for their next opportunity.

## 🚀 Live Demo & Frontend
- **Live Demo**: [JobScout Web App](https://jobscout-app.netlify.app/)
- **Frontend Repository**: [JobScout-Frontend](https://github.com/Hamzathul-karrar/JobScout-Frontend)

## ✨ Key Features
- **Secure Authentication**: Implementation of JWT-based authentication with access and refresh tokens.
- **Job Search Integration**: Utilizes SerpAPI to fetch real-time job listings.
- **Database Management**: MySQL database integration using Spring Data JPA with Hibernate.
- **Robust Logging**: Comprehensive logging configuration (rolling file appender) to keep track of application events and errors.
- **Environment Profiles**: Configured with multiple profiles (e.g., `application-dev.properties` and production `application.properties`) for flexible deployments.

## 🛠 Technologies Used
- **[Java 17](https://jdk.java.net/17/)**: Core programming language.
- **[Spring Boot 3.4.0](https://spring.io/projects/spring-boot)**: Framework for building the REST API.
- **[Spring Security](https://spring.io/projects/spring-security)**: Security and authentication.
- **[Spring Data JPA](https://spring.io/projects/spring-data-jpa)**: ORM and database interactions.
- **[MySQL](https://www.mysql.com/)**: Relational Database Management System.
- **[JJWT](https://github.com/jwtk/jjwt)**: Java JWT library for token generation and validation.
- **Maven**: Dependency management and build tool.

## 📁 Project Structure

```text
src/main/java/com/hamza/Jobscout/
├── config/         # Configuration classes (Security, CORS, etc.)
├── controller/     # REST API controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA Entities
├── exception/      # Global exception handlers
├── repository/     # Spring Data JPA repositories
├── service/        # Business logic layer
└── JobScoutApplication.java # Application entry point
```

## 🚦 Getting Started

### Prerequisites
- JDK 17 or higher
- Maven 3.6+
- MySQL Server

### Installation
1. **Clone the repository:**
   ```bash
   git clone https://github.com/Hamzathul-karrar/JobScout-Backend.git
   cd JobScout-Backend
   ```

2. **Configure the Database:**
   Ensure MySQL is running and create a database named `jobscout` (if using dev properties). Update the properties in `src/main/resources/application-dev.properties` or set your environment variables for production in `application.properties`:
   - `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`
   - `MYSQLUSER`, `MYSQLPASSWORD`
   - `JWT_SECRET`

3. **Configure SerpAPI:**
   The application uses SerpAPI for fetching jobs. Update `serpapi.api.key` in your `application-dev.properties` with your actual SerpAPI key.

4. **Build and Run the Application:**
   Using Maven wrapper:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```
   Or using standard Maven:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

The backend server will start at `http://localhost:8080` (or `8082` for dev profile).
