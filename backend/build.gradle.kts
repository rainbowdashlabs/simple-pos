import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    java
    alias(libs.plugins.spring.boot)
}

group = "dev.chojo"
version = "0.1.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.rest)
    implementation(libs.spring.session.core)
    implementation(libs.postgresql)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.bundles.spring.security)
    implementation(libs.springdoc.openapi)

    implementation(libs.jjwt.api)
    runtimeOnly(libs.bundles.jjwt.runtime)

    testImplementation(platform(SpringBootPlugin.BOM_COORDINATES))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.bundles.testcontainers)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.processResources {
    val projectVersion = project.version.toString()
    inputs.property("projectVersion", projectVersion)
    from(sourceSets.main.get().resources.srcDirs) {
        filesMatching("version") {
            var version = projectVersion
            val workflow = (System.getenv("GITHUB_ACTIONS") ?: "false") == "true"
            if (workflow) {
                val now = ZonedDateTime.now(ZoneOffset.UTC)
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val formattedDate = now.format(formatter)

                version = when (System.getenv("GITHUB_REF_TYPE")) {
                    "branch" -> "$version ${System.getenv("GITHUB_REF_NAME")}-${
                        System.getenv("GITHUB_SHA").substring(0, 7)
                    } @ $formattedDate"

                    "tag" -> "$version ${System.getenv("GITHUB_REF_NAME").substring(1)} @ $formattedDate"
                    else -> "$version snapshot"
                }
            }
            expand(
                "version" to version
            )
        }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
