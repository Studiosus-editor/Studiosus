# Studiosus

A web-based system that helps to write Ansible scripts

_Team members:_

- Čepulis Nerijus
- Maciūnas Adrijanas Juozas
- Petkevičius Lukas

_Supervisor_:

- dr. Bukauskas Linas

## Project vision

The main goal of this project is to create a web-based system that helps to write Ansible scripts. The system will provide a user-friendly interface for writing, testing, and running Ansible scripts. The system will also provide a way to manage Ansible scripts, save them, and share them with other users. The system will be designed to be easy to use and will provide a way to write Ansible scripts without having to know the Ansible syntax.

## Project Documentation

### Table of Contents

- [Contribution Guidelines](./doc/CONTRIBUTING.md)
  If you're interested in contributing to this project, please refer to this section for essential information and guidelines.

## Technology stack

- **Frontend**:
  - **Svelte** - a JavaScript framework for building user interfaces
  - **Sass** - a CSS preprocessor that helps to write more maintainable and scalable stylesheets    

- **Backend**:
  - **Spring Boot** - an open-source Java-based framework used to create a micro Service
  - **Gradle** - a build automation tool that is designed to support multi-project builds
  - **Lombok** - a Java library that automatically plugs into your editor and build tools, spicing up your java
  - **Google Vertex API** - Interaction with Googles Gemini API
  - **Docker** - a platform for developing, shipping, and running applications
  
- **Database**:
  - **H2** - an in-memory database that provides Java SQL and JDBC API

## How to run program (Devolopment)

**Node version v20.11.1** or newer is needed to compile!

You can install **NodeJS** from [here](https://nodejs.org/en/download/)
or by using Node version manager **nvm**, by running:

    cd frontend
    nvm use

#### Start the frontend server (Terminal 1)
    cd frontend
    npm run dev

**Java version 21** or newer is needed to compile!

You can install **Java** from [here](https://www.oracle.com/java/technologies/downloads/#java21)
or by using **sdkman**

    sdk install java 21.0.2-open

#### Start the backend server (Terminal 2):
    cd backend
    ./gradlew bootRun

After these steps the webserver should be acessible at `http://localhost:3000`

## How to run program (Production)

**Docker** is needed!
You can install docker from [here](https://docs.docker.com/get-docker/)

Our application is dockerized and can be run using `docker-compose.yml` file.
Both `/frontend` and `/backend` have a coresponding `DockerFile`, which is used to generate a **Docker image**.

Currently the application will start with 2 prefilled users:

- **Admin**:
  - username: DEMO user
  - email: user@studiosus.lt
  - password: pass

- **User**:
  - username: ADMIN user
  - email: admin@studiosus.lt
  - password: pass

If you want to change these `user credentials` you can create an `.env` file inside
root of the project where `docker-compose.yml` file is present with following values:

    PREFILL_username=your_username
    PREFILL_email=your_email
    PREFILL_password=your_password
    PREFILL_username2=your_username2
    PREFILL_email2=your_email2
    PREFILL_password2=your_password2

### Start the application

    docker-compose up --build -d

After these steps the webserver should be accessible at `http://localhost:80`