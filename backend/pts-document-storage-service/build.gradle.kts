plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("jacoco")
}

group = "${rootProject.group}.document.storage"
version = rootProject.version

dependencies {
    implementation(libs.spring.web)
    implementation(libs.spring.actuator)
    implementation(libs.spring.validation)

    implementation(libs.spring.amqp)
    implementation(libs.kafka)

    implementation(libs.lombok)
    implementation(libs.dotenv)

    implementation(libs.s3)
    runtimeOnly(libs.postgresql)

    implementation(libs.springdoc)

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation(libs.spring.test)
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("org.pts.document.storage.DocumentStorageApplication")
}