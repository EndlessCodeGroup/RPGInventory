## Read more about ProGuard configuration:
## https://www.guardsquare.com/en/products/proguard/manual

## You can comment it out to enable obfuscation, but with wrong configuration it can break your plugin
-dontobfuscate
-dontoptimize

## Some configurations for obfuscation
#-printmapping out.map
#-keepparameternames
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

## Repackage all shadowed classes
#-repackageclasses shadow.ru.endlesscode.rpginventory

## We need to keep all plugin's classes.
-keep class ru.endlesscode.rpginventory.*

## Keep events listeners
-keep class * implements org.bukkit.event.Listener {
    @org.bukkit.event.EventHandler public void *;
}

-keep class * implements org.bukkit.entity.Entity {
    ** getHandle();
}

## Rules for Inspector
-keep class * extends org.bukkit.event.Event { org.bukkit.event.HandlerList getHandlerList(); }

## Some common rules
-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

-keepclassmembers enum * {
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
