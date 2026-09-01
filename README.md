![Banner](fastlane/metadata/android/en-US/images/featureGraphic.png)

# Transport Widget

An app that provides home screen widgets displaying live arrival times of public transit services, supports multiple data source.

Utilize [Transitous](https://transitous.org/) as global data source, data feed directly from [Transport for London](https://api-portal.tfl.gov.uk/) and [Rail Delivery Group](https://raildata.org.uk/) also provided.

Supports Android 7 to 16.

## Getting the App
Android (F-Droid)                            |	Android (Release Page)
-------------------------------------------------------:|:-------------------------------------------------------
[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/com.eden.livewidget/) |	[Release Page](https://github.com/someone-s/transport-widget/releases)

## How do I...?
Refer to the individual help page on the [Wiki](https://someone-s.github.io/transport-widget/)


## Screenshots


|                                            Widget |                 Config Screen                  |                  Home Screen                   | About Screen                                 |
|--------------------------------------------------:|:----------------------------------------------:|:----------------------------------------------:|:---------------------------------------------|
| ![Provider Screen](fastlane/metadata/android/en-US/images/phoneScreenshots/1.png) | ![Config Screen](fastlane/metadata/android/en-US/images/phoneScreenshots/3.png) | ![Home Screen](fastlane/metadata/android/en-US/images/phoneScreenshots/2.png) | ![About Screen](/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png) |

## Building From Source
Android Studio is the environment used for development, however, the app can also be built from the command line.

Gradle has to be installed.
```
gradlew :app:assembleRelease
```

## Data Source Acknowledgement
Refer to the individual provider page on the [Wiki](https://someone-s.github.io/transport-widget/)