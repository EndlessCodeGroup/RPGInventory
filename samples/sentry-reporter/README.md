# Sentry Reporter
Reporter that sends reports to [Sentry](https://sentry.io/).  

### Gradle
```groovy
ext.inspectorVersion = "0.9"
dependencies {
    implementation "ru.endlesscode.inspector:inspector-sentry-reporter:$inspectorVersion"
    implementation "ru.endlesscode.inspector:sentry-bukkit:$inspectorVersion" // If you want BukkitPluginSentryClientFactory
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
            // If you want more detailed reports, add this, but you also should
            // add `sentry-bukkit` dependency before
            .setClientFactory(new BukkitPluginSentryClientFactory(this))
            .focusOn(this) // this - instance of TrackedPlugin
            .build();
}
```
Setting DSN is required.
There are also available `setDataSourceName(String dsn)` if you want just use DSN link instead.
