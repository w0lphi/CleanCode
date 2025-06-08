plugins {
    id("java")
    id("jacoco")
    id("application")
}

group = "org.aau"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.aau.WebCrawlerService")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // oder 21, 11, etc.
    }
}

jacoco {
    toolVersion = "0.8.10"
}

tasks.test {
    systemProperty("project.buildDir", layout.buildDirectory.get().asFile.absolutePath)
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

dependencies {
    implementation("org.seleniumhq.selenium:selenium-java:4.31.0")
    implementation("org.seleniumhq.selenium:selenium-devtools-v135:4.31.0")
    implementation("org.jsoup:jsoup:1.17.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mock-server:mockserver-junit-jupiter-no-dependencies:5.14.0")
    testImplementation("org.mock-server:mockserver-client-java-no-dependencies:5.14.0")

}