plugins {
    kotlin("jvm") version "1.4.10"
}

group = "app.mainichi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.amazonaws:aws-java-sdk-s3:1.11.906")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
}
