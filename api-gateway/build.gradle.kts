plugins {
	java
	id("org.springframework.boot") version "3.3.1"
	id("io.spring.dependency-management") version "1.1.5"
}

group = "com.worldline.easypay"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

extra["springCloudVersion"] = "2023.0.2"

dependencies {
	// Spring Cloud API Gateway
	implementation("org.springframework.cloud:spring-cloud-starter-gateway")
	implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

	// Spring Cloud Config client
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	// Spring Cloud Service Discovery
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

	// Expose metrics with Micrometer using a Prometheus registry
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.micrometer:micrometer-registry-prometheus")

	// Logging JSON support
	implementation("ch.qos.logback.contrib:logback-json-classic:0.1.5")
	implementation("ch.qos.logback.contrib:logback-jackson:0.1.5")

	// Opentelemetry exemplars support (metrics)
	implementation(platform("io.opentelemetry:opentelemetry-bom:1.38.0"))
	implementation("io.opentelemetry:opentelemetry-api")
	implementation("io.prometheus:prometheus-metrics-tracer-otel-agent:1.3.1")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")

	developmentOnly("org.springframework.boot:spring-boot-devtools")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}