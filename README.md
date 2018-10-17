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


### Java Example
```java
public class Main {
    public static void main(String[] args) {
        VialServer.create()
                .get("/", ((request, responseBuilder) ->
                        responseBuilder.setBodyJson(new Pojo("hello GET"))))
                .post("/", ((request, responseBuilder) ->
                        responseBuilder.setBodyJson(new Pojo("hello POST"))))
                .get("/v1/foo/:who/fifi", ((request, responseBuilder) -> {
                        String who = request.pathParam("who").orElse("unknown");
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
