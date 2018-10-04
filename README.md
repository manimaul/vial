# Vial
### Simple Netty based http/1.1 and http/2 framework
[![CircleCI](https://circleci.com/gh/manimaul/vial/tree/master.svg?style=svg)](https://circleci.com/gh/manimaul/vial/tree/master)

### Gradle
```groovy
repositories {
    maven { url  "https://dl.bintray.com/madrona/maven" }
}

dependencies {
    implementation group: 'com.willkamp', name: 'vial-server', version: '0.0.4'
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
