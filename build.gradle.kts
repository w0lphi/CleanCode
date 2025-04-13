plugins {
    id("java")
}

group = "org.aau"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // oder 21, 11, etc.
    }
}

dependencies {
    implementation("org.seleniumhq.selenium:selenium-java:4.31.0")
    //implementation("io.github.bonigarcia:webdrivermanager:5.5.3")
    implementation("org.seleniumhq.selenium:selenium-devtools-v135:4.31.0")
    implementation("org.jsoup:jsoup:1.17.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}