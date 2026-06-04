FROM gradle:8.14-jdk21 AS build

WORKDIR /workspace

COPY settings.gradle.kts build.gradle.kts ./
COPY pts-document-storage-service/build.gradle.kts pts-document-storage-service/build.gradle.kts
RUN gradle --no-daemon :pts-document-storage-service:dependencies

COPY pts-document-storage-service pts-document-storage-service
RUN gradle --no-daemon :pts-document-storage-service:bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN addgroup --system pts && adduser --system --ingroup pts pts

COPY --from=build /workspace/pts-document-storage-service/build/libs/*.jar app.jar

USER pts

EXPOSE 8094

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
