plugins {
    groovy
    `java-library`
}

dependencies {
    api(project(":ontrack-json"))
    api(project(":ontrack-common"))
    api(project(":ontrack-job"))
    
    implementation("com.google.guava:guava")
    implementation("org.apache.commons:commons-text")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.security:spring-security-core")
    implementation("javax.validation:validation-api")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("org.codehaus.groovy:groovy-all")
    testImplementation(project(":ontrack-test-utils"))

}

val testJar by tasks.registering(Jar::class) {
    classifier = "tests"
    from(sourceSets["test"].allJava)
}

tasks["assemble"].dependsOn("testJar")

val tests by configurations.creating

artifacts {
    add("tests", testJar)
}