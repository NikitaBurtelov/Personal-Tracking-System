import org.gradle.api.tasks.testing.Test

plugins {
    id("java")
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
    id("com.palantir.git-version") version "5.0.0"
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
    apply(plugin = "java")
    apply(plugin = "jacoco")

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.0")
        }
    }

    dependencies {
        val libs = rootProject.extensions
            .getByType(VersionCatalogsExtension::class)
            .named("libs")

        implementation(libs.findLibrary("spring-logging").get())

        implementation(libs.findLibrary("util-dotenv").get())
        implementation(libs.findLibrary("util-micrometer").get())

        testImplementation(libs.findLibrary("spring-test").get())
        testImplementation(libs.findLibrary("test-junit").get())
        testImplementation(libs.findLibrary("test-mockito").get())
    }

    tasks.register("printVersion") {
        description = "project version"
        doLast {
            println("${project.name}: ${project.version}")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}