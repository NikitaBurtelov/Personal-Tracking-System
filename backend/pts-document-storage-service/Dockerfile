FROM gradle:9.3.0-jdk21 AS build

WORKDIR /workspace

COPY . .
RUN ./gradlew --no-daemon :pts-document-storage-service:dependencies

RUN ./gradlew --no-daemon :pts-document-storage-service:bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN addgroup --system --gid 10001 pts && adduser --system --uid 10001 --ingroup pts pts

COPY --from=build /workspace/pts-document-storage-service/build/libs/*.jar app.jar

USER 10001:10001

EXPOSE 8094

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
