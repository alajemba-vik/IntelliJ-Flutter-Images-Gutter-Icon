# Flutter Images Gutter Icon 🎨

[![Version](https://img.shields.io/jetbrains/plugin/v/25155.svg)](https://plugins.jetbrains.com/plugin/25155)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/25155.svg)](https://plugins.jetbrains.com/plugin/25155)

<!-- Plugin description -->
This IntelliJ plugin allows images from the assets folder to be visible in the gutter for Flutter projects.
It supports bitmap formats like **png** and **jpeg** and vector file formats like **svg**.
<!-- Plugin description end -->

## 🚀 How to install?

You can install the plugin directly from Android Studio:
1. Open _Settings_
2. Choose _Plugins_
3. Search in _Marketplace_ for **Flutter Images Gutter Icon**
4. Install

## How to use?

Once the plugin is installed, provide your file pattern in the settings for your IDE:
1. Navigate to _Settings > Tools > Flutter Images Gutter Icon_
2. Provide the image file patterns as needed
3. Apply the settings and your project will be updated automatically 💃🏾
   
<br>

## 🎉 What's new?

Check out the [CHANGELOG](CHANGELOG.md).

<br>

## 🌟 Would really like to improve this plugin?

### Prerequisites
- IntelliJ IDEA
- JDK 17
- Gradle
- Knowledge about Kotlin
- Knowledge about building plugins for the IntelliJ Platform or the willingness to dig around the [documentation](https://plugins.jetbrains.com/docs/intellij/creating-plugin-project.html)
  
### Configuration
During development, this plugin was run using Android studio, so this is set up as the default IDE:
```
//build.gradle.kts

runIde {
        // IDE Development Instance (the "Contents" directory is macOS specific, update the path to your IDE's directory):
        ideDir.set(file(System.getenv("ANDROID_STUDIO_DIR")))
    }
```
You need to set `ANDROID_STUDIO_DIR` as an environment variable.

You can quickly set this up in the run configuration of the IntelliJ IDEA:

1. Go to _Run > Edit Configurations..._
2. Select the run configuration for your project
3. In _Run/Debug Configurations_, find the _Environment variables_ field

💡 You can easily find the path of the Android studio you are using to run your flutter project by running `flutter doctor -v`
and checking out the information under the _Android toolchain - develop for Android devices_ section.

### 🙌🏾 Wrote some awesome code, have some suggestions or found an annoying bug 🐛?

Feel free to send a [Pull Request](https://github.com/alajemba-vik/IntelliJ-Flutter-Gutter-Image-Viewer/pulls) or file a new [Issue](https://github.com/alajemba-vik/IntelliJ-Flutter-Gutter-Image-Viewer/issues)!

### 📃 License
This project is licensed under the BSD 3-Clause License - see the [LICENSE](LICENSE) file for details.
