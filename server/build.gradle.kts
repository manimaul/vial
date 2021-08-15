plugins {
    `java-library`
    `maven-publish`
}

tasks.register("verifyGithubConfig")  {
    doLast {
        println("github_user: ${project.findProperty("github_user")}")
        println("github_user: ${project.findProperty("github_token")}")
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/manimaul/vial")
            credentials {
                username = "${project.findProperty("github_user")}"
                password = "${project.findProperty("github_token")}"
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            groupId = "com.willkamp"
            artifactId = "vial-server"
            version = "${project.version}"

            from(components["java"])
        }
    }
}
