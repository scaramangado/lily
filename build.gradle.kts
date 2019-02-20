plugins {
   `java-library`
}

group = "de.scaramanga"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    api("org.springframework.boot:spring-boot-starter:2.1.2.RELEASE")

    implementation("org.reflections:reflections:0.9.11")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test:2.1.2.RELEASE") {
        exclude(module = "junit")
        exclude(module = "hamcrest-library")
        exclude(module = "hamcrest-core")
        exclude(module = "json-path")
        exclude(module = "jsonassert")
        exclude(module = "xmlunit-core")
    }

    testImplementation("org.assertj:assertj-core:3.11.1")
}

tasks.withType(Test::class) {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.compileTestJava {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()
}

tasks.withType(Wrapper::class) {
    gradleVersion = "5.1.1"
}
