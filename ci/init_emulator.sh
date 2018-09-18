#!/usr/bin/env bash
adb shell settings put global window_animation_scale 0 &
adb shell settings put global transition_animation_scale 0 &
adb shell settings put global animator_duration_scale 0 &
adb shell pm grant cz.josefadamcik.trackontrakt.dev android.permission.READ_EXTERNAL_STORAGE
adb shell pm grant cz.josefadamcik.trackontrakt.dev android.permission.WRITE_EXTERNAL_STORAGE
adb shell input keyevent 82 &
