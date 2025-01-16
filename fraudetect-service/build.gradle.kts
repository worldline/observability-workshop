import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
	java
	alias(libs.plugins.springBoot)
}

group = "com.worldline.easypay"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(platform(SpringBootPlugin.BOM_COORDINATES))
	implementation(platform(libs.spring.cloud.bom))
	implementation(platform(libs.opentelemetry.instrumentation.bom))

	// Spring Boot Web
	implementation("org.springframework.boot:spring-boot-starter-web")

	// ORM for database access: JPA & Postgres
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("org.postgresql:postgresql")

	// Spring Cloud Config client
	implementation("org.springframework.cloud:spring-cloud-starter-config")

	// Spring Cloud service discovery client
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

	// Spring Cloud Stream (kafka)
	implementation("org.springframework.cloud:spring-cloud-stream")
	implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka")
	implementation("org.springframework.kafka:spring-kafka")

	// Spring Boot Management and Monitoring
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.testcontainers:kafka")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
