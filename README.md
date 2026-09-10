![Banner](fastlane/metadata/android/en-US/images/featureGraphic.png)

# Transport Widget

Keep track of departures or arrivals that matters to you, on your home screen. Transport Widget is a open source widget app that display live departure and arrival times of public transit services, utilizing various public open data source.

Big thanks to [Transitous](https://transitous.org/) for acting as a global data source, the app also support gathering data directly from [Transport for London](https://api-portal.tfl.gov.uk/) and [Rail Delivery Group](https://raildata.org.uk/).

Supports Android 7 to 16.

## Screenshots

|                                           |                               |                                   |                                | |
|--------------------------------------------------:|:----------------------------------------------:|:----------------------------------------------:|:---------------------------------------------|:---------------------------------------------|
| ![1](fastlane/metadata/android/en-US/images/phoneScreenshots/1.png) | ![2](fastlane/metadata/android/en-US/images/phoneScreenshots/2.png) | ![3](fastlane/metadata/android/en-US/images/phoneScreenshots/3.png) | ![4](/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png) | ![5](/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png) |

## Getting the App
Android (F-Droid)                            |	Android (Play Store)                            |	Android (Release Page)
-------------------------------------------------------:|:-------------------------------------------------------:|:-------------------------------------------------------
[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/com.eden.livewidget/) | [Closed Testing Google Group](https://groups.google.com/g/transport-widget-closed-testing) | [Release Page](https://github.com/someone-s/transport-widget/releases)

I'm in the process of getting the app published on Play Store.

I am looking for people to participate in closed testing to meet [Google's requirements](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en-GB) to publish the app.

Join the [Google Group](https://groups.google.com/g/transport-widget-closed-testing) and follow the instructions within to help with the publishing process.

## How do I...?
Refer to the individual help page on the [Wiki](https://someone-s.github.io/transport-widget/)


## Building From Source
Android Studio is the environment used for development, however, the app can also be built from the command line.

Gradle has to be installed.
```
gradlew :app:assembleRelease
```

## Data Source Acknowledgement
Refer to the individual provider page on the [Wiki](https://someone-s.github.io/transport-widget/)
