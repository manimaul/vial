import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("net.researchgate.release") version "2.8.1"
    id("org.jetbrains.kotlin.jvm") version "1.3.41"
}

release {
    tagTemplate = "'v$version'"
    versionPropertyFile = "gradle.properties"
    scmAdapters = listOf(net.researchgate.release.GitAdapter::class.java)
}

project.logger.info("jdk version is : ${JavaVersion.current()}")
if (!JavaVersion.current().isJava11Compatible) {
    throw GradleException("The minimum build jdk version required is ${JavaVersion.VERSION_11}")
}

allprojects {
    apply(plugin= "java")
    apply(plugin= "org.jetbrains.kotlin.jvm")

    repositories {
        jcenter()
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "${JavaVersion.VERSION_1_8}"
    }

    // enable JUnit5
    tasks.test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    val slf4jVersion="1.7.25"
    val groovyVersion="2.4.1"
    val logbackVersion="1.2.3"
    val nettyVersion="4.1.37.Final"
    val nettyBoringSslVersion="2.0.25.Final"
    val typesafeConfigVersion="1.3.2"
    val jacksonVersion="2.9.4"
    val guavaVersion="22.0"

    // testing dependencies
    val hamcrestVersion="1.3"
    val mockitoVersion="2.18.3"
    val junitVersion="5.2.0"

    dependencies {
        // Kotlin
        api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        // Netty
        implementation("io.netty:netty-all:$nettyVersion")
        implementation("io.netty:netty-tcnative-boringssl-static:$nettyBoringSslVersion")
        implementation("io.netty:netty-codec-http:$nettyVersion")

        // Logging
        implementation("org.slf4j:slf4j-api:$slf4jVersion")
        implementation("ch.qos.logback:logback-core:$logbackVersion")
        implementation("ch.qos.logback:logback-classic:$logbackVersion")
        implementation("org.codehaus.groovy:groovy-all:$groovyVersion")

        // Jackson
        implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
        implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

        implementation("com.google.guava:guava:$guavaVersion")
        implementation("com.typesafe:config:$typesafeConfigVersion")

        // JUnit5
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testImplementation("org.mockito:mockito-core:$mockitoVersion")
        testImplementation("org.hamcrest:hamcrest-all:$hamcrestVersion")
        testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    }
}

// todo: (WK) reenable
//tasks.afterReleaseBuild {
//    dependsOn(":server:bintrayUpload")
//}

/* Install/Upgrade the Gradle wrapper
./gradlew wrapper --gradle-version=5.5 --distribution-type=bin
 */
