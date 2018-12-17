-libraryjars <java.home>/lib/rt.jar
-printmapping out.map
-verbose

-dontobfuscate
-keepparameternames
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-repackageclasses ru.endlesscode.inspector.shade

# We don't need to obfuscate or minify any classes except shade
-keep,includedescriptorclasses class !ru.endlesscode.inspector.shade.**  {
    *;
}

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# We use method getHandlerList() to inject our handler to all events.
-keep class * extends org.bukkit.event.Event { org.bukkit.event.HandlerList getHandlerList(); }
