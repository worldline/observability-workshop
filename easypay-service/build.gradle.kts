plugins {
	java
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.4"
}

group = "com.worldline.easypay"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

extra["springCloudVersion"] = "2023.0.1"
extra["springDocVersion"] = "2.5.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
	// implementation("io.github.danielliu1123:httpexchange-spring-boot-starter:3.2.5")
	implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
	
	implementation("org.springframework.cloud:spring-cloud-stream")
	implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka")
	implementation("org.springframework.kafka:spring-kafka")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springDocVersion")}")

	// Expose metrics with Micrometer using a Prometheus registry
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.micrometer:micrometer-registry-prometheus")

	// Add opentelemetry exemplars support
	implementation(platform("io.opentelemetry:opentelemetry-bom:1.38.0"))
	implementation("io.opentelemetry:opentelemetry-api")
	implementation("io.prometheus:prometheus-metrics-tracer-otel-agent:1.3.1")

	// Logging
	implementation("ch.qos.logback.contrib:logback-json-classic:0.1.5")
	implementation("ch.qos.logback.contrib:logback-jackson:0.1.5")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}