-libraryjars <java.home>/lib/rt.jar
-printmapping out.map

-keepparameternames
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keep,includedescriptorclasses public class !ru.endlesscode.inspector.shade.**,ru.endlesscode.inspector.**  {
      public protected *;
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

-dontwarn com.google.**
-dontwarn com.sun.**
-dontwarn org.slf4j.**
-dontwarn org.dom4j.**
-dontwarn javax.servlet.**
-dontwarn javax.annotation.**
-dontwarn ru.endlesscode.inspector.shade.org.reflections.vfs.**
