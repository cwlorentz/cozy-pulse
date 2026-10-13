# Cozy Pulse

A cozy Android app for watching how your heart rate settles during ASMR videos.

- Tap **Begin** (optionally name the video), switch to your video, come back and tap **Finish session**.
- Tap **✨ Tingles!** whenever you feel them; those moments show as lavender dots on the chart.
- Each session gets a soft heart rate chart, average / lowest / highest bpm, how much your heart
  slowed from the first fifth to the last fifth, and when you were calmest.
- Everything is stored on the phone only.

## Where the heart rate comes from

Cozy Pulse reads heart rate from **Health Connect**, which most Android watch apps write into
(Samsung Health for Galaxy Watch, Fitbit, Pixel Watch, Garmin Connect, and others). Make sure your
watch app has Health Connect syncing turned on for heart rate.

Watches often sync to the phone in batches, so the live number during a session can lag or stay
empty. That's fine: when you finish, the app re-reads the whole session window, and it re-syncs
automatically for a couple of hours afterwards (or tap the refresh button on a session).

## Building

Requirements: Android Studio (or JDK 17 + Android SDK 35).

```
./gradlew assembleRelease
```

The APK lands in `app/build/outputs/apk/release/`. It's signed with the debug key so you can
sideload it directly (allow "Install unknown apps" for your file manager or browser).

Pushing this folder to a GitHub repo also builds the APK automatically: open the repo's
**Actions** tab, pick the latest "Build APK" run, and download the `CozyPulse-apk` artifact.

## Possible next step

A small Wear OS companion app could stream heart rate live from the watch instead of waiting
for Health Connect to sync. That only works on Wear OS watches (Galaxy Watch 4+, Pixel Watch).
