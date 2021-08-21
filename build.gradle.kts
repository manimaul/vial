import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.21"
    id("net.researchgate.release") version "2.8.1"
    `java-library`
}

release {
    tagTemplate = "v$version"
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
        mavenCentral()
        google()
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

    val slf4jVersion="1.7.32"
    val groovyVersion="3.0.8"
    val logbackVersion="1.2.5"
    val nettyVersion="4.1.66.Final"
    val nettyBoringSslVersion="2.0.40.Final"
    val typesafeConfigVersion="1.4.1"
    val jacksonVersion="2.12.4"

    // testing dependencies
    val hamcrestVersion="1.3"
    val mockitoVersion="2.18.3"
    val junitVersion="5.7.1"

    dependencies {
        // Kotlin
        api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        api(platform("org.jetbrains.kotlin:kotlin-bom"))

        // Netty
        api("io.netty:netty-all:$nettyVersion")
        api("io.netty:netty-tcnative-boringssl-static:$nettyBoringSslVersion")
        api("io.netty:netty-codec-http:$nettyVersion")

        // Logging
        api("org.slf4j:slf4j-api:$slf4jVersion")
        api("ch.qos.logback:logback-core:$logbackVersion")
        api("ch.qos.logback:logback-classic:$logbackVersion")
        api("org.codehaus.groovy:groovy-all:$groovyVersion")

        // Jackson
        api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
        api("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
        api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

        api("com.typesafe:config:$typesafeConfigVersion")

        // JUnit5
        testImplementation(platform("org.junit:junit-bom:$junitVersion"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.mockito:mockito-core:$mockitoVersion")
        testImplementation("org.hamcrest:hamcrest-all:$hamcrestVersion")
    }
}

tasks.afterReleaseBuild {
    dependsOn(":server:publish")
}
