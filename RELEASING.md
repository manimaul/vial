# Releasing

#### Set your Github user and token
```
vim ~/.gradle/gradle.properties

github_user=<your_user>
github_token=<your_token>
```

#### Tag release and publish

```
$./gradlew :server:release
```