# Sentry Reporter
Reporter that sends reports to [Sentry](https://sentry.io/).  

### Gradle
```groovy
dependencies {
    implementation "ru.endlesscode.inspector:inspector-sentry-reporter:0.8.0"
}
```

## Usage
You should return `SentryReporter` in `createReporter` method:
```java
@Override
protected Reporter createReporter() {
    String publicKey = "[YOUR_PUBLIC_KEY_HERE]";
    String projectId = "[YOUR_PROJECT_ID_HERE]";

    return new SentryReporter.Builder()
            .setDataSourceName(publicKey, projectId)
            .focusOn(this) // this - instance of TrackedPlugin
            .build();
}
```
Setting DSN is required.
There are also available `setDataSourceName(String dsn)` if you want just use DSN link instead.
