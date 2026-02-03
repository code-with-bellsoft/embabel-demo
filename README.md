# Embabel Demo App

A Spring Boot application demonstrating the use of Embabel Agent Framework.


## Prerequisites

- Java 17+
- Docker and Docker Compose

## Tech stack

- MongoDB
- Maven
- Spring Shell
- Embabel
- Llama 3.1:8b

## Running the Application

### Running Locally

1. Start the MongoDB container:
   ```shell
   docker-compose up -d
   ```
2. Run the llama model:
    ```shell
   ollama run llama3.1:8b
    ```
3. Run the app:
   ```shell
   ./mvnw spring-boot:run
   ```