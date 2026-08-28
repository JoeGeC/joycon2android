# Keep readable stack traces in release crash reports, without giving up
# obfuscation of everything else (R8 still emits mapping.txt to de-obfuscate).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Persisted button-mapping enums.
# ControllerMappingDataStore writes DataStore keys as "<Console>|<JoyconSide>|<target>"
# and values as JoyconButton/StickSource names; MappingConversions reads them back with
# enumValueOf / name matching. R8 would otherwise be free to rewrite these constant names,
# which stays self-consistent within one build but drifts between releases — silently
# wiping every saved mapping on upgrade. Pin the names so the on-disk contract is stable.
-keepnames enum com.joegec.joycon2android.buttonmapping.** { *; }
-keepnames enum com.joegec.joycon2android.model.JoyconButton { *; }
