# Keep only the neutral JNI bridge required for dynamic native registration.
-keep,allowoptimization class com.chand.mobiletina.k.Q {
    native <methods>;
}

# Android/WorkManager instantiate these components outside normal direct call paths.
-keep class com.chand.mobiletina.widget.** extends android.appwidget.AppWidgetProvider { *; }
-keep class com.chand.mobiletina.work.PriceUpdateWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Stronger symbol/package flattening for distributable builds.
-repackageclasses 'c'
-allowaccessmodification
-adaptclassstrings
-renamesourcefileattribute SourceFile

# Keep metadata required by Kotlin/Compose/WorkManager while still allowing class renaming.
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault,Signature,InnerClasses,EnclosingMethod
