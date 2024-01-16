plugins {
    // id("org.jetbrains.kotlin.jvm") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("io.projectreactor:reactor-core:3.5.4")
    implementation("io.vertx:vertx-jdbc-client:4.4.0")
    implementation("io.agroal:agroal-pool:1.16")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.mysql:mysql-connector-j:8.0.32")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    // implementation("com.google.guava:guava:31.0.1-jre")

    // Use the Kotlin test library.
    // testImplementation("org.jetbrains.kotlin:kotlin-test")
    // testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClass.set("insertest.App2")
}
