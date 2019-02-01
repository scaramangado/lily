plugins {
   `java-library`
}

group = "de.pieroavola"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.wrapper {
    gradleVersion = "5.1.1"
}
