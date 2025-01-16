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

	// ORM database access: JPA & Postgres
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("org.postgresql:postgresql")

	// Spring Boot GraphQL support (to be implemented)
	implementation("org.springframework.boot:spring-boot-starter-graphql")

	// Spring Cloud Streams (Kafka)
	implementation("org.apache.kafka:kafka-streams")
	implementation("org.springframework.cloud:spring-cloud-stream")
	implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka")
	implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka-streams")
	implementation("org.springframework.kafka:spring-kafka")

	// Spring Cloud Config client
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	
	// Spring Cloud service discovery client
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

	// Spring Boot Management and Monitoring
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework:spring-webflux")
	testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder")
	testImplementation("org.springframework.graphql:spring-graphql-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	testImplementation("org.testcontainers:postgresql")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
