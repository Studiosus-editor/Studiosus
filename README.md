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

## To start the live server

**Java version 21** or newer is needed to compile!

#### Run the program using Gradle:

    cd backend
    ./gradlew bootRun

#### Or compile the program and then run:


    cd backend

On Linux, MacOS & Windows versions that support `shell` scripts

    ./gradlew build

On Windows

    gradlew.bat build

Then

    java -jar build/libs/studiosus.jar

After these steps the webserver should be acessible at `localhost:8080`
