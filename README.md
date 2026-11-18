# AS ANAMUL VOICE — Android source starter

A clean-room Kotlin + Jetpack Compose starter for the Android app. The design uses a dark RGB/neon visual theme and the supplied ANAMUL MODZ logo as a reference asset. The app display name is **AS ANAMUL VOICE**.

## Included now
- Android project structure and GitHub Actions debug build workflow.
- Home, Voice Library, Soundboard, Text-to-Voice and Settings navigation.
- Access-key verification API client.
- Voice-list and TTS API client calls.
- Audio file picker entry point.
- Floating overlay permission and status service.
- Telegram support link.
- Original APK feature map and implementation-status checklist.

## Not complete / needs integration
This is a starter, not a finished production clone. Coupon redemption, `.isfile` import/export, card editing, full soundboard queue, video extraction, MediaStore export, update installer flow, production auth, and provider-specific TTS adapters need implementation and tests. Root/Shizuku-based hidden microphone injection into third-party games is deliberately not implemented; Android has no universal public API for this.

## Build with GitHub
1. Create a new GitHub repository.
2. Upload the contents of this folder (not the outer folder itself if you want the workflow at repository root).
3. Open **Actions** and run **Android Build** or push to `main`.
4. Download the `AS-ANAMUL-VOICE-debug` artifact.

The workflow uses Gradle 8.9, JDK 17 and Android SDK 35. A signed release build needs your own signing key and GitHub secrets; never commit the keystore or passwords.

## Configure backend URL
Edit `app/src/main/java/com/anamul/voice/net/ApiClient.kt` and set the HTTPS API base URL after Muse AI deploys the backend. Do not put TTS provider secrets in the Android app.

## Reference
See `FEATURES_AND_IMPLEMENTATION_STATUS.md` and `ORIGINAL_APK_ANALYSIS.md`.
