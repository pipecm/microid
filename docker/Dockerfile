FROM eclipse-temurin:latest as build
ADD . /app
WORKDIR /app
RUN ./gradlew clean build -Dprofile=default

FROM eclipse-temurin:latest as service
COPY --from=build /app/build/libs/microid-0.0.1-SNAPSHOT.jar /app/microid-app-0.0.1-SNAPSHOT.jar
RUN chmod +x /app/microid-app-0.0.1-SNAPSHOT.jar && adduser microid
USER microid
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/microid-app-0.0.1-SNAPSHOT.jar"]