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

	// Spring Cloud Config
	implementation("org.springframework.cloud:spring-cloud-starter-config")

	// Spring Cloud API Gateway
	implementation("org.springframework.cloud:spring-cloud-starter-gateway")
	implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

	// Spring Boot Management and Monitoring
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Spring Cloud Service Discovery
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}