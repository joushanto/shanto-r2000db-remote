# Shanto’s R2000DB Remote

An unofficial Android infrared remote for **Edifier R2000DB speakers**, using the **RC10D** command set. Includes the correct input selectors and dedicated **Classic / Dynamic EQ** controls.

**Tested successfully by Shanto on a Redmi Note 9S with an Edifier R2000DB.** All ten controls were confirmed working on 31 October 2026. Other phones require a compatible built-in infrared emitter and have not been tested.

Built upon [Edifier-RC10D1](https://github.com/norbicki/Edifier-RC10D1) by **Norbert Rybicki**. The original Android app, its NEC transmitter implementation, and its artwork provided the foundation for this adaptation. R2000DB command definitions come from the **Home Assistant Team’s [infrared-protocols](https://github.com/home-assistant-libs/infrared-protocols)** library. Full attribution and pinned source references appear below.

Application ID: `com.shanto.r2000dbremote`. The original RC10D1 app can remain installed.

## Why this version exists

The original RC10D1 app targets the R2730DB. Its power, mute, and volume commands also work with the R2000DB, but input commands differ: for example, its Line 1 button selects optical on an R2000DB. This adaptation corrects those mappings, removes the unsupported coax input, adds both EQ modes, and provides labeled buttons in a scrollable interface.

The app communicates directly through the phone’s infrared emitter. It requires no Home Assistant installation, Bluetooth pairing, account, or internet connection. It requests the Android infrared transmission permission; there is no internet permission.

## Controls

| Button | NEC command | Complete frame |
| --- | --- | --- |
| Power | 00 | FF00E710 |
| Mute | 01 | FE01E710 |
| Volume + | 09 | F609E710 |
| Volume − | 0C | F30CE710 |
| Line 1 | 0D | F20DE710 |
| Line 2 | 16 | E916E710 |
| Optical | 0A | F50AE710 |
| Bluetooth | 15 | EA15E710 |
| Classic EQ | 0E | F10EE710 |
| Dynamic EQ | 14 | EB14E710 |

All commands use NEC extended address `0xE710`, least-significant bit first. The carrier (38028 Hz) and pulse timings are preserved from the upstream Android app. The Coax button has been removed because the R2000DB command set has no coax input.

The complete frame column is written as a hexadecimal integer; bits are transmitted least-significant first. No Python package is needed at runtime: this Android app contains the command constants and NEC encoder.

## Install and use

1. Obtain an APK built from this source, or build it using the steps below.
2. Copy it to your Android phone and open it. If prompted, allow the app you opened it with to install this APK.
3. Launch **Shanto’s R2000DB Remote** and point the phone’s infrared emitter at the speaker.
4. Tap a control. EQ changes can be easier to hear while music is playing.

A “Sent” message confirms that transmission was requested, not that the speaker received it. Infrared is one-way: the app cannot display the actual input, EQ mode, volume level, or power state. Phones without an emitter show a message and disable the controls.

The initial v1.0 APK is a **debug-signed build for personal testing**. A fresh build on another computer may use a different debug signing key; Android will then require uninstalling the previous copy before installing it. To distribute stable updates, use a dedicated release signing key and keep that key private and backed up.

## Build

### Prerequisites

- JDK **17**; set `JAVA_HOME` to its installation directory.
- Android SDK **platform 34**, **build-tools 34.0.0**, and platform-tools. Android Studio’s SDK Manager can install these.
- Set `ANDROID_HOME` to the SDK directory, or create an untracked `local.properties` containing `sdk.dir=C:/path/to/Android/Sdk` (use your actual path).
- Internet access on the first build to download Gradle and Maven dependencies.

The repository includes the Gradle **8.2** wrapper. The project uses Android Gradle Plugin **8.2.0** and Kotlin **1.8.0**; a global Gradle installation is unnecessary. Minimum Android version is **5.0 / API 21**; compile and target SDK are **34**.

### Command line

Clone your copy and enter the project folder:

```bash
git clone https://github.com/joushanto/shanto-r2000db-remote.git
cd shanto-r2000db-remote
```

Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

macOS / Linux:

```bash
chmod +x gradlew
./gradlew assembleDebug testDebugUnitTest lintDebug
```

The installable APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

With USB debugging enabled and your phone connected, you can install it with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Alternatively, open this repository in Android Studio, let Gradle sync, select the `app` configuration and your device, and click **Run**. For a signed release, use **Build → Generate Signed Bundle / APK → APK**, create or select your own keystore, and keep the keystore and passwords out of Git.

### Validation

The initial version passed `assembleDebug`, `testDebugUnitTest`, and `lintDebug` (zero lint errors; dependency/resource warnings remain). The protocol unit test checks all ten expected frames and independently decodes the generated pulse sequence to verify bit order and timings. The mappings were also compared against the pinned R2000DB reference. Shanto then confirmed every button on the actual speaker.

Test reports: `app/build/reports/tests/testDebugUnitTest/index.html`. Lint report: `app/build/reports/lint-results-debug.html`.

## Source layout

- `app/src/main/java/ir/remote/edifier/rc10d1/MainActivity.kt` — button bindings and IR transmission.
- `app/src/main/java/ir/remote/edifier/rc10d1/InfraredProtocol.kt` — NEC frame and pulse encoder.
- `app/src/main/res/layout/activity_main.xml` — remote interface.
- `app/src/main/res/values/strings.xml` — app name and button labels.
- `app/src/test/java/ir/remote/edifier/rc10d1/InfraredProtocolTest.kt` — protocol checks.
- `app/src/main/assets/` — bundled license and attribution notices.

The source directory names retain the upstream layout; Kotlin declarations and the application namespace use `com.shanto.r2000dbremote`.

## Troubleshooting

- **Nothing happens:** confirm your phone has an IR emitter, point it directly at the speaker, and move closer. Bluetooth support alone is insufficient.
- **Installation reports a signature conflict:** use the same signing key as the installed build, or uninstall that copy before installing your newly signed build.
- **SDK location not found:** configure `ANDROID_HOME` or `local.properties` as described above.
- **Gradle reports the wrong Java version:** verify `java -version` and `JAVA_HOME`, or select JDK 17 in Android Studio’s Gradle settings.
- **Inputs select the wrong source:** confirm you are running this R2000DB version, rather than the original RC10D1 app. Other speaker models can use different mappings.

## Credits and licenses

### Original Android application

**Norbert Rybicki**, copyright © 2023. [Edifier-RC10D1 source](https://github.com/norbicki/Edifier-RC10D1), also available through [F-Droid](https://f-droid.org/en/packages/ir.remote.edifier.rc10d1/). Adapted from commit [`416c46f`](https://github.com/norbicki/Edifier-RC10D1/commit/416c46f28a8ae0d6dd42670c4f8d23544b8624aa). Licensed under **GNU GPL v3.0 or later**; see [LICENSE](LICENSE). The original author’s commit history is available in the upstream repository.

### Infrared command definitions

**Home Assistant Team**, copyright © 2026. [infrared-protocols R2000DB definitions](https://github.com/home-assistant-libs/infrared-protocols/blob/68e5256bbb855d09bfa8c82f05b221425238ff09/infrared_protocols/codes/edifier/r2000db.py), pinned to commit `68e5256bbb855d09bfa8c82f05b221425238ff09`. Licensed under **MIT**; the full notice is retained in [THIRD_PARTY_LICENSES.txt](THIRD_PARTY_LICENSES.txt) and bundled in the APK.

### This adaptation

Customized for **Shanto Jouerder** with corrected R2000DB input mappings, Classic/Dynamic EQ buttons, a separate application identity, revised interface, and protocol tests. Modifications made on 31 October 2026 with Codex assistance. This adaptation remains **GPL-3.0-or-later**; preserve the original attribution and applicable license notices when redistributing it.

This is an unofficial project, not affiliated with or endorsed by Edifier, the original app author, or Home Assistant. Edifier product names belong to their respective owners.
