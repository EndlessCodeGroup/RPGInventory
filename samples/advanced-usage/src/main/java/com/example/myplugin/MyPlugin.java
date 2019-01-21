package com.example.myplugin;

import ru.endlesscode.inspector.report.DiscordReporter;
import ru.endlesscode.inspector.report.Reporter;
import ru.endlesscode.inspector.bukkit.plugin.TrackedPlugin;

public class MyPlugin extends TrackedPlugin {

    public MyPlugin() {
        super(MyPluginLifecycle.class);
    }

    @Override
    protected Reporter createReporter() {
        // Here we construct the reporter, that we want to use
        return new DiscordReporter.Builder()
                .hook("530363177725329428", "TSjeyavper8NhIbnLBVeUSzxi5OgBMMyXrlDomE_Tv7iOqgTFRMQ1PA7x_bKnIHKjrAf")
                .focusOn(this)
                .build();
    }

}
