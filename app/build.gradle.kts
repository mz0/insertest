plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.mysql:mysql-connector-j:8.0.32")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
}

application {
    mainClass.set("insertest.App2")
}
