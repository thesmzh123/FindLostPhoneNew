# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#Specifies not to ignore non-public library classes.
-dontskipnonpubliclibraryclasses

#Specifies not to ignore package visible library class members
-dontskipnonpubliclibraryclassmembers

-optimizationpasses 5
#Specifies that the access modifiers of classes and class members may have become broad during processing. This can improve the results of the optimization step.
-allowaccessmodification
#Specifies that interfaces may be merged, even if their implementing classes don't implement all interface methods. This can reduce the size of the output by reducing the total number of classes.
-mergeinterfacesaggressively

#Specifies to apply aggressive overloading while obfuscating. Multiple fields and methods can then get the same names, This option can make the processed code even smaller
#-overloadaggressively

#Specifies to repackage all packages that are renamed, by moving them into the single given parent package
-flattenpackagehierarchy

#Specifies to repackage all class files that are renamed, by moving them into the single given package. Without argument or with an empty string (''), the package is removed completely.
-repackageclasses

#For example, if your code contains a large number of hard-coded strings that refer to classes, and you prefer not to keep their names, you may want to use this option
-adaptclassstrings
#Specifies the resource files to be renamed, all resource files that correspond to class files are renamed
-adaptresourcefilenames

#Specifies the resource files whose contents are to be updated. Any class names mentioned in the resource files are renamed
-adaptresourcefilecontents

#Specifies not to verify the processed class files.
#-dontpreverify

-verbose

#Specifies to print any warnings about unresolved references and other important problems, but to continue processing in any case.
-ignorewarnings

# ADDED
#-dontobfuscate
#-useuniqueclassmembernames

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.fragment.app.Fragment{}
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable
-keep class device.spotter.finder.appss.models.MapView
#-keep class com.google.firebase.* { *; }
