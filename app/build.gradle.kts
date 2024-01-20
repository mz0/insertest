plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.projectreactor:reactor-core:3.6.2")
    implementation("io.vertx:vertx-jdbc-client:4.4.0")
    implementation("io.agroal:agroal-pool:2.2")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.mysql:mysql-connector-j:8.0.32")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
}

application {
    mainClass.set("insertest.App2")
}
