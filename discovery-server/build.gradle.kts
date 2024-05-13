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

dependencies {
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
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
