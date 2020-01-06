package com.example.myplugin;

import ru.endlesscode.inspector.bukkit.plugin.TrackedPlugin;
import ru.endlesscode.inspector.report.Reporter;
import ru.endlesscode.inspector.report.SentryReporter;
import ru.endlesscode.inspector.sentry.bukkit.BukkitPluginSentryClientFactory;

public class MyPlugin extends TrackedPlugin {

    public MyPlugin() {
        super(MyPluginLifecycle.class);
    }

    @Override
    protected Reporter createReporter() {
        String publicKey = "845550f6ac0946c9bae87217906aa8e5";
        String projectId = "1331962";

        return new SentryReporter.Builder()
                .setDataSourceName(publicKey, projectId)
                .setClientFactory(new BukkitPluginSentryClientFactory(this))
                .focusOn(this)
                .build();
    }

}
