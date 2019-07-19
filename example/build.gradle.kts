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
    mainClassName = "com.willkamp.MainKt"
}

//repositories {
//    maven { setUrl( "https://dl.bintray.com/madrona/maven") }
//}

dependencies {
    implementation(project(":server"))
//    implementation("com.willkamp:vial-server:0.0.5")
}
