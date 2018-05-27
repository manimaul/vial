# Vial
### Simple Netty based http/1.1 and http/2 framework

### Gradle
```groovy
repositories {
    maven { url  "https://dl.bintray.com/madrona/maven" }
}

dependencies {
    implementation group: 'com.willkamp', name: 'vial-server', version: '0.0.2'
}
```

### Example
```kotlin
private val log = LoggerFactory.getLogger(Main::class.java)

data class Response(val message: String = "OK")

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        log.debug("starting example server")

        Server(port = 8080)
                .get("/", { response ->
                    response.setJson(pojo = Response())
                }).serve()
    }
}
```
