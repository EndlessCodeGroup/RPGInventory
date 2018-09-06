# Inspector

![Inspector Example](https://gitlab.com/endlesscodegroup/inspector/raw/develop/images/example.png)  
![Event Logging Example](https://gitlab.com/endlesscodegroup/inspector/raw/develop/images/event_log_example.png)

Inspector helps developers to track all exceptions and crashes of theirs plugins.
It automatically sends reports to the developer with all needed information about the environment.

It sends:
- Plugin name and version
- Server core and version
- List of plugins with versions
- Exception stacktrace

Also it contains some tools:
- EventLogger - tool to log events
- PacketsLogger - tool to log packets (needs ProtocolLib)

## For server owners
Just copy plugin to `plugins/` folder.
Also you can disable sending of information about server core and installed plugins in the Inspector's config.

### Config example
```yaml
Reporter:
  enabled: true 
  # Here you can choose what you don't want to send
  data:
    core: true    # Info about server core
    plugins: true # Plugins list

# Events logger it is the tool that helps developers to debug events
EventsLogger:
  enabled: false
  # What we need to log
  # Here you can use super classes to configure event groups
  log:
  - Event                               # Log all events
  - PlayerStatisticIncrementEvent:100   # Skip this event 100 times
  - PlayerMoveEvent:20                  
  - -ChunkEvent                         # Don't log all events that extends ChunkEvent
  - -BlockEvent                          
  - -VehicleEvent                       
  - -EntityAirChangeEvent               # Don't log the event

# Events logger it is the tool that helps developers to debug events
PacketsLogger:
  enabled: false
  # What we need to log
  # Format: <protocol>[.<source>[.<name>]]
  # Minecraft protocol: https://wiki.vg/Protocol
  # ProtocolLib packet types: https://github.com/aadnk/ProtocolLib/blob/master/modules/API/src/main/java/com/comphenix/protocol/PacketType.java
  log:
  - Handshake                           # Log all protocols
  - Status
  - Login
  - Play
  - Play.Client.POSITION:20             # Skip this packet 20 times
  - -Play.Client.LOOK                   # Don't want these frequent events
  - -Play.Server.MAP_CHUNK
  - -Play.Server.UPDATE_TIME
  - -Play.Server.ENTITY_HEAD_ROTATION
  - -Play.Server.ENTITY_VELOCITY
  - -Play.Server.ENTITY_TELEPORT
  - -Play.Server.ENTITY_METADATA
  - -Play.Server.ENTITY_LOOK
  - -Play.Server.ENTITY_STATUS
  - -Play.Server.REL_ENTITY_MOVE
  - -Play.Server.REL_ENTITY_MOVE_LOOK
```

## For plugin developers

To add Inspector to the plugin you should:
- Add Inspector as a dependency to the project
- Add Inspector to plugin.yml to `depend` section
- Modify main plugin class
- Change main class in `plugin.yml` to new

### Add Inspector to the project
```groovy
repositories { 
    maven { 
        url "https://dl.bintray.com/endlesscode/repo" 
    } 
}

dependencies {
    compileOnly "ru.endlesscode.inspector:inspector-bukkit:0.5.0"
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
should become:
```java
public class MyPlugin extends PluginLifecycle {
    // ...
    // onEnable, onDisable, etc.
    // ...
}
```

And then you should create the new class extending `TrackedPlugin` that will be used as main and linked with the lifecycle.
Also you should override method `createReporter()`. Created reporter will be used for reporting errors.  
Example:
```java
public class MyTrackedPlugin extends TrackedPlugin {
    
    public MyTrackedPlugin() {
        super(MyPlugin.class); // Pass here lifecycle class
    }
    
    @Override
    public Reporter createReporter() {
        return new DiscordReporter.Builder()
                .hook("<DISCORD_WEBHOOK_ID>", "<DISCORD_WEBHOOK_TOKEN>")
                .focusOn(this) // Reporter will be focused on this plugin
                .build();
        // For more reporter customization see DiscordReporter.Builder and Reporter.Builder classes
    }
}
```
You can see example of plugin migration here: endlesscodegroup/rpginventory/rpginventory@33bca425

> **NOTE:** At this moment available only Discord Reporter, but will be added more reporters soon.
