# Inspector

![Inspector Example](https://gitlab.com/endlesscodegroup/inspector/raw/develop/images/example.png)

Inspector helps developers to track all exceptions and crashes of theirs plugins.
It automatically sends reports to the developer with all needed information about the environment.

It sends:
- Plugin name and version
- Server core and version
- List of plugins with versions
- Exception stacktrace

## For server owners
Just copy plugin to `plugins/` folder.

## For plugin developers

To add support of Inspector to the plugin you should:
- Add Inspector as a dependency to the project
- Add Inspector to plugin.yml to `depend` section
- Modify main plugin class
- Change main class in `plugin.yml` to new

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
should be:
```java
public class MyPlugin extends PluginLifecycle {
    // ...
    // onEnable, onDisable, etc.
    // ...
    
    // If you target on server version lower than 1.12, you should 
    // override method getDatabase()
    public EbeanServer getDatabase() {
        return holder.getDatabase();
    }
}
```

And then you should create the new class extending `TrackedPlugin` that will be used as main and linked with the lifecycle.
Also you should override method `createReporter()`. Created reporter will be used for reporting errors.  
Example:
```java
public class MyTrackedPlugin extends TrackedPlugin {
    
    public MyTrackedPlugin() {
        super(MyPlugin.class);
    }
    
    @Override
    public Reporter createReporter() {
        return DiscordReporter(
                this, // Reporter will be focused on this plugin
                "<DISCORD_WEBHOOK_ID>",
                "<DISCORD_WEBHOOK_TOKEN>"
        );
    }
}
```
You can see example of plugin migration here: endlesscodegroup/rpginventory/rpginventory@0270322f

> **NOTE:** At this moment available only Discord Reporter, but will be added more reporters soon.
