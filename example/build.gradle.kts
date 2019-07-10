import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin(module="jvm") version("1.3.41")
}

application {
    mainClassName = "com.willkamp.MainKt"
}

repositories {
    maven { setUrl( "https://dl.bintray.com/madrona/maven") }
    jcenter()
}

dependencies {
    compile(project(":server"))
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
