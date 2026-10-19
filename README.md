# ECE-FlashMaster — App Download Guide

An Android app for making reviewers (flashcards) to help students revise key concepts.

This README explains how to download and install the APK, how to get the app from official stores or Releases, how to build from source, and troubleshooting tips.

## Download options

1. Google Play Store (recommended)
   - If the app is published on the Play Store, search for "ECE-FlashMaster" or use the direct Play Store link and install it as any other app.
   - Replace this section with the Play Store URL if available.

2. GitHub Releases / APK (recommended if Play Store not available)
   - Look for a release in this repository under the "Releases" tab. Download the APK attached to the latest release.
   - Direct APK URL (example placeholder): https://github.com/Kireppu/ECE-FlashMaster/releases/download/vX.Y.Z/ECE-FlashMaster-vX.Y.Z.apk
   - Replace the placeholder URL above with the real release asset link if you publish one.

3. Build from source
   - Clone the repository:
     ```bash
     git clone https://github.com/Kireppu/ECE-FlashMaster.git
     cd ECE-FlashMaster
     ```
   - Build a debug APK:
     ```bash
     ./gradlew assembleDebug
     # or on Windows
     gradlew.bat assembleDebug
     ```
   - Build a release APK (requires signing config):
     ```bash
     ./gradlew assembleRelease
     ```
   - Output APKs are in `app/build/outputs/apk/`.

## Installing the APK on an Android device

1. Prepare the device
   - Android 8.0 (Oreo) and later: you must allow the app (installer) to install unknown apps. Typically, this is done when you open the APK from your file manager or browser and follow the prompt to allow that source.
   - Older Android versions: enable "Install unknown apps" or "Unknown sources" in Settings → Security.

2. Transfer and install
   - Transfer the APK to your device (USB, cloud, or direct download). Open the APK file and follow the installer prompts.

3. Permissions
   - The app will ask for runtime permissions required for its features. Grant only the permissions you are comfortable with.

4. Uninstall previous versions
   - If you have a previous installation from a different signing key, you may need to uninstall the old version before installing a new APK.

## Verifying the APK

- Only install APKs from trusted sources. If you host APKs in Releases, ensure you sign them with a release key and publish checksums (SHA256) so users can verify integrity:
  ```bash
  sha256sum ECE-FlashMaster-vX.Y.Z.apk
  ```
- If published on the Play Store, the store handles signing and distribution.

## Troubleshooting

- App fails to install: check Android version and whether the APK is corrupted. Re-download the APK.
- App crashes on launch: please open an issue in this repository with the crash log (Logcat) and reproduce steps.
- Permissions denied: go to Settings → Apps → ECE-FlashMaster → Permissions and enable the needed permissions.

## Build & signing notes for maintainers

- Configure signing in `app/build.gradle` or in a `gradle.properties` file with a signingConfigs block for release builds.
- For CI builds, set up secure storage for the keystore and signing keys.

Example signing configuration (replace placeholders):
```groovy
android {
  signingConfigs {
    release {
      storeFile file("keystore.jks")
      storePassword System.getenv("KEYSTORE_PASSWORD")
      keyAlias System.getenv("KEY_ALIAS")
      keyPassword System.getenv("KEY_PASSWORD")
    }
  }
}
```

## Contact / Support

If you have issues downloading or installing the app, open an issue in this repository or contact the maintainer.

---

Note for repository owner: replace placeholder Play Store and Releases URLs with real links, and add any store badges or screenshots if desired.
