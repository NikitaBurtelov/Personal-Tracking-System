import org.gradle.api.tasks.testing.Test

plugins {
    id("java")
    id("java-library")
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

group = "org.pts"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.0")
    }
}

subprojects {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "java-library")
    apply(plugin = "jacoco")

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.0")
        }
    }

    dependencies {
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
        testImplementation("org.mockito:mockito-core:5.12.0")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}