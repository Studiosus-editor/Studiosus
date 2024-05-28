# Stage 1: Build the application
FROM gradle:jdk21 AS build
WORKDIR /home/gradle/src
COPY . .
ENV GITHUB_REDIRECT_URI=https://api.studiosus.live:8443/login/oauth2/code/github
RUN ./gradlew bootJar
ENV spring.profiles.active=production

# Stage 2: Copy the built application from the build stage and create the Docker image
FROM eclipse-temurin:21-jre
COPY --from=build /home/gradle/src/build/libs/*.jar /app.jar
COPY --from=build /home/gradle/src/src/main/resources/gemini-api.json /gemini-api.json
ENV GITHUB_REDIRECT_URI=https://api.studiosus.live:8443/login/oauth2/code/github
ENV GOOGLE_APPLICATION_CREDENTIALS=/gemini-api.json
ENV spring.profiles.active=production
ENTRYPOINT ["java","-jar","/app.jar"]