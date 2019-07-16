plugins {
    application
    kotlin(module="jvm")
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
