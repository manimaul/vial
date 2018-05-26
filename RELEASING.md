# Releasing

#### Set your bintray user and key
```
vim ~/.gradle/gradle.properties

bintray_user=your_user
bintray_key=your_key
```

#### Tag release and push to bintray

```
$ ./gradlew release
```