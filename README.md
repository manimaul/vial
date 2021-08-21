# Vial
### Simple Netty based http/1.1 and http/2 framework
[![CircleCI](https://circleci.com/gh/manimaul/vial/tree/master.svg?style=svg)](https://circleci.com/gh/manimaul/vial/tree/master)

### Gradle

Edit $HOME/.gradle/gradle.properties and add your Github user and token
```text
github_user=<your_username>
github_token=<your_token>
```

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/manimaul/vial")
        credentials {
            username = "${project.findProperty("github_user")}"
            password = "${project.findProperty("github_token")}"
        }
    }
}

dependencies {
    implementation("com.willkamp:vial-server:2.0.1")
}
```

### Kotlin Example
```kotlin
fun main() {
    VialServer.create()
            .httpGet("/") { request ->
                request.respondWith { responseBuilder ->
                    responseBuilder.setBodyJson(Pojo("hello GET"))
                }
            }
            .httpPost("/") { request ->
                request.respondWith { responseBuilder ->
                    responseBuilder.setBodyJson(Pojo("hello POST"))
                }
            }
            .addHandler(FooHandler())
            .webSocket("/websocket") { webSocket ->
                webSocket.sendText("hello")
                webSocket.receiveText {
                    println("received message = $it")
                }
            }
            .listenAndServeBlocking()
}

class FooHandler : EndPointHandler {
    override val route = "/v1/foo/:who/fifi"

    override fun handle(request: Request) {
        val who = request.pathParam("who") ?: ("unknown")
        request.respondWith { builder ->
            builder.setBodyJson(Pojo("hello GET foo - who = $who"))
        }
    }
}


private data class Pojo(
        val message: String
)
```

### Java Example
```java
class Main {
    public static void main(String[] args) {
        VialServer.create()
            .httpGet("/", request ->
                request.respondWith(response ->
                    response.setBodyJson(new Pojo("hello GET"))
                )
            )
            .httpPost("/", request ->
                request.respondWith(response ->
                    response.setBodyJson(new Pojo("hello POST"))
                )
            )
            .httpGet("/v1/foo/:who/fifi", request -> {
                String who = request.pathParamOption("who").orElse("unknown");
                request.respondWith(response ->
                    response.setBodyJson(new Pojo(String.format("hello GET foo - who = %s", who)))
                );
            })
            .webSocket("/websocket", (webSocket) -> {
                webSocket.sendText("hello");
                webSocket.receiveText(msg -> System.out.printf("received message = %s%n", msg));
            })
            .listenAndServeBlocking();
    }

    static class Pojo {
        private final String message;

        Pojo(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
```
