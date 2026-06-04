FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/*.jar pts-document-storage-service-1.0.0.jar

ENTRYPOINT ["java", "-jar", "pts-document-storage-service-1.0.0.jar"]