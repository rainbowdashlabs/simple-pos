plugins {
    java
    alias(libs.plugins.spring.boot)
}

group = "dev.chojo"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))

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

    testImplementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.bundles.testcontainers)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
