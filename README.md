# Vial
### Simple Netty based http/1.1 and http/2 framework

Example
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