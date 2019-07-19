# Vial
### Simple Netty based http/1.1 and http/2 framework
[![CircleCI](https://circleci.com/gh/manimaul/vial/tree/master.svg?style=svg)](https://circleci.com/gh/manimaul/vial/tree/master)

### Gradle
```groovy
repositories {
    maven { url  "https://dl.bintray.com/madrona/maven" }
}

dependencies {
    implementation group: 'com.willkamp', name: 'vial-server', version: '0.0.5'
}
```

### Kotlin Example
```kotlin
fun main() {

    VialServer.create()
            .httpGet("/") { _, responseBuilder: ResponseBuilder ->
                responseBuilder.setBodyJson(Pojo("hello GET"))
            }
            .httpPost("/") { _, responseBuilder ->
                responseBuilder.setBodyJson(Pojo("hello POST"))
            }
            .httpGet("/v1/foo/:who/fifi") { request, responseBuilder ->
                val who = request.pathParam("who")?:("unknown")
                responseBuilder.setBodyJson(Pojo("hello GET foo - who = $who"))
            }
            .listenAndServeBlocking()
}

private data class Pojo(
        val message: String
)
```

### Java Example
```java
class Main {
    public static void main(String[] args) {
        VialServer.Companion.create()
                .httpGet("/", ((request, responseBuilder) ->
                        responseBuilder.setBodyJson(new Pojo("hello GET"))))
                .httpPost("/", ((request, responseBuilder) ->
                        responseBuilder.setBodyJson(new Pojo("hello POST"))))
                .httpGet("/v1/foo/:who/fifi", ((request, responseBuilder) -> {
                    String who = request.pathParamOption("who").orElse("unknown");
                    return responseBuilder.setBodyJson(
                            new Pojo(String.format("hello GET foo - who = %s", who)));
                }))
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
