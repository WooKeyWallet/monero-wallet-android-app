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

-ignorewarnings

# 保留源代码行号
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 一般可以加上这两个规则，不跳过非公开库的类成员，保留方法上的异常属性
-dontskipnonpubliclibraryclassmembers
-keepattributes Exceptions

-dontwarn kotlin.**

# Keep android classes in AndroidManifest.xml
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Fragement
-keep public class * extends android.support.v4.app.Fragment

-keep public class * extends android.support.v4.** { *; }
-keep public class * extends android.support.v7.** { *; }

-keep class android.support.** { *; }
-keep interface android.support.** { *; }
-dontwarn android.support.**

-keep public class * extends android.view.View {
      public <init>(android.content.Context);
      public <init>(android.content.Context, android.util.AttributeSet);
      public <init>(android.content.Context, android.util.AttributeSet, int);
      public void set*(...);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}

-keep class android.support.annotation.** { *; }
-keep interface android.support.annotation.** { *; }

-keepclassmembers enum * {
     public static **[] values();
     public static ** valueOf(java.lang.String);
}

# 混淆包名
-repackageclasses ''
-allowaccessmodification

# 不混淆 Serializable 相关内容
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 实体类
-keep class **.data.**{*;}

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# okhttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**

# Okio
-keep class sun.misc.Unsafe { *; }
-keep class okio.**{*;}
-keep interface okio.**{*;}
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class com.sunloto.shandong.bean.** { *; }

# EventBus
-keepclassmembers class ** {
    public void onEvent*(***);
}
# Only required if you use AsyncExecutor
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    public <init>(java.lang.Throwable);
}
# Don't warn for missing support classes
-dontwarn de.greenrobot.event.util.*$Support
-dontwarn de.greenrobot.event.util.*$SupportManagerFragment

# zxing
-dontwarn com.google.zxing.**
-keep class com.google.zxing.** { *; }
-keepclasseswithmembernames class * { native <methods>; }

# webview
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
       public *;
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.webView, java.lang.String);
}

# timber
-dontwarn org.jetbrains.annotations.**

#Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}