plugins {
    application
    kotlin(module="jvm")
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClass.set("com.willkamp.MainKt")
}

dependencies {
    implementation(project(":server"))
}
