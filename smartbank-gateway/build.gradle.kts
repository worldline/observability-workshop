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

extra["springDocVersion"] = "2.5.0"

dependencies {
	implementation(platform(SpringBootPlugin.BOM_COORDINATES))
	implementation(platform(libs.spring.cloud.bom))
	implementation(platform(libs.opentelemetry.instrumentation.bom))

	// Spring Boot Web
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// ORM database access: JPA & Postgres
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("org.postgresql:postgresql")

	// Spring Cloud Config client
	implementation("org.springframework.cloud:spring-cloud-starter-config")

	// Spring Cloud service discovery client
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

	// Spring Boot Management and Monitoring
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Add caching support
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("com.hazelcast:hazelcast-all:4.2.8")

	// OpenAPI
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springDocVersion")}")


	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.testcontainers:postgresql")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
