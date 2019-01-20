# Inspector

![Inspector Example](https://gitlab.com/endlesscodegroup/inspector/raw/develop/images/example.png)  

Inspector helps developers to track all exceptions and crashes of theirs plugins.
It automatically sends reports to the developer with all needed information about the environment.

It sends not sensitive data:
- Plugin name and version
- Version of Inspector
- Exception stacktrace
- Unique ID of server (it can't be used to determine who sent a report, it used only to determine that "two reports sent from same server")

Also, it sends some sensitive data that can be disabled from sending:
- Server core and version
- List of plugins with theirs versions

## For server owners
This is not a plugin and can't be installed with copying to `plugins` directory.
But you can configure it.
You can disable sending of information about server core and installed plugins in the `inspector.yml` that stored in directory of the each plugin that uses Inspector.
Also you can configure it globally in `plugins/Inspector/config.yml`.

### Config example
```yaml
Reporter:
  enabled: true 
  # Here you can choose what you don't want to send
  data:
    core: true    # Info about server core
    plugins: true # Plugins list
```

## For plugin developers

To add Inspector to the plugin you must:
- Add Inspector as a dependency to the project
- Modify main plugin class
- Change main class in `plugin.yml` to new

Also, for more coverage, you should:
- Change all usages of `BukkitRunnable` to `TrackedBukkitRunnable`
- Wrap `CommandExecutor` with `TrackedCommandExecutor` 

### Add Inspector to the project
```groovy
plugins {
    // Add shadow plugin to make shadowJar
    // See: http://imperceptiblethoughts.com/shadow/
    id 'com.github.johnrengelman.shadow' version '4.0.1'
}

// Inspector published at jcenter, so we need to add it to repositories
repositories { 
    jcenter() 
}

// Enable shadowJar minimization to reduce plugin size.
// Read more: https://imperceptiblethoughts.com/shadow/configuration/minimizing/
shadowJar {
    minimize()
}

// To avoid possible conflicts we should relocate embedded dependencies to own unique package
// Easiest variant is use automatically relocating
// Read more: https://imperceptiblethoughts.com/shadow/configuration/relocation/#automatically-relocating-dependencies
task relocateShadowJar(type: ConfigureShadowRelocation) {
    target = tasks.shadowJar
    prefix = "shadow.[PLACE_HERE_YOUR_PLUGIN_PACKAGE]"
}
tasks.shadowJar.dependsOn tasks.relocateShadowJar
// Automatically run shadowJar making on every assemble
tasks.build.assemble tasks.shadowJar

// Here you can change preferred version of inspector
ext.inspectorVerson = "0.8.0"

// Add Inspector as dependency
// 'inspector-bukkit' - implementation of Inspector for Bukkit.
// 'inspector-sentry-reporter' - reporter that we want to use (read below about available reporters)
dependencies {
    implementation "ru.endlesscode.inspector:inspector-bukkit:$inspectorVerson"
    implementation "ru.endlesscode.inspector:inspector-sentry-reporter:$inspectorVerson"
}
```

### Main plugin class modifications

First of all your current main plugin class should extend `PluginLifecycle` instead of `JavaPlugin`.  
For example, this code:
```java
public class MyPlugin extends JavaPlugin {
    // ...
    // onEnable, onDisable, etc.
    // ...
}
```
must become:
```java
public class MyPlugin extends PluginLifecycle {
    // ...
    // onEnable, onDisable, etc.
    // ... 
}
```

If you doing in constructor any work that requires access to plugin's fields you can receive `UninitializedPropertyAccessException`.
To avoid it override method init() and do the work within:
```java
public class MyPlugin extends PluginLifecycle {
    @Override
    public void init() {
        // do some work
    } 
}
```

When previous action done, you must create the new class extending `TrackedPlugin` that will be used as main plugin class and link it with the lifecycle.
Also you must override method `createReporter()`. Created reporter will be used for reporting errors.  
Example:
```java
public class MyTrackedPlugin extends TrackedPlugin {
    
    public MyTrackedPlugin() {
        super(MyPlugin.class); // Pass here lifecycle class
    }
    
    @Override
    public Reporter createReporter() {
        String publicKey = "[YOUR_PUBLIC_KEY_HERE]";
        String projectId = "[YOUR_PROJECT_ID_HERE]";

        // Note that you should add needed reporter as dependency first.
        return new SentryReporter.Builder()
                .setDataSourceName(publicKey, projectId)
                .focusOn(this) // Reporter will be focused on this plugin
                .build();
    }
}
```

#### Available Reporters
- [inspector-sentry-reporter](inspector-sentry-reporter): Report exceptions to [Sentry](https://sentry.io/) *(recommended way)*
- DiscordReporter: Send reports to Discord channel
