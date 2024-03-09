# Studiosus

A web-based system that helps to write Ansible scripts

_Team members:_

- Čepulis Nerijus
- Maciūnas Adrijanas Juozas
- Petkevičius Lukas

_Supervisor_:

- dr. Bukauskas Linas

## Project vision

TBA

## Project Documentation

### Table of Contents

- [Contribution Guidelines](./doc/CONTRIBUTING.md)
  If you're interested in contributing to this project, please refer to this section for essential information and guidelines.

## Development dependencies

These dependencies are automatically included in our program:

TBA

## How to run program (Devolopment)

**Java version 21** or newer is needed to compile!

#### Start the frontend server (Terminal 1)
    cd frontend
    npm run dev

#### Start the backend server (Terminal 2):
    cd backend
    ./gradlew bootRun

After these steps the webserver should be acessible at `localhost:8080`

## How to compile program into JAR

**Java version 21** or newer is needed to compile!

#### Compile the Jar

    cd backend
    ./gradlew bootJar

#### Run the jar

    java -jar backend/build/libs/studiosus.jar

After these steps the webserver should be acessible at `localhost:8080`

## How to generate a docker container (Production)

**Java version 21** or newer is needed to compile!

#### Generate a docker image

    cd backend
    ./gradlew bootBuildImage

#### Start docker container with generated image 

    docker run -it -p 8080:8080 studiosus

After these steps the webserver should be acessible at `localhost:8080`