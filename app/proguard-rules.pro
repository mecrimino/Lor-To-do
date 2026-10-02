# Proguard rules for LOR TO-DO
# Keep Room entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Glance AppWidgets
-keep class * extends androidx.glance.appwidget.GlanceAppWidget
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver
