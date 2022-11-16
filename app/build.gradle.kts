plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.mysql:mysql-connector-j:8.0.31")
    implementation("org.apache.logging.log4j:log4j-core:2.18.0")
    implementation("org.apache.logging.log4j:log4j-api:2.18.0")
}

application {
    mainClass.set("insertest.App2")
}
