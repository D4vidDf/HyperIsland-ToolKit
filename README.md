# HyperIsland Kit 🏝️
<img alt="Version 0.1.2" src="https://img.shields.io/badge/version-0.2.0-blue"/>

A simple, fluent Kotlin builder for creating notifications on Xiaomi's HyperIsland.

This library abstracts away the complex JSON and `Bundle` linking, allowing you to build rich HyperIsland notifications with clean, readable Kotlin.

## Features
* Fluent, chained Kotlin builder
* No more manual JSON building
* Automatically handles all `miui.focus.action_` prefixing
* Full support for all core features:
    * Action Buttons (Icon, Text, or Progress)
    * Timers (Countdown & Count-Up)
    * Progress Bars (Linear & Circular)
    * Custom Icons & Colors

---

## 📚 Documentation (Read This!)

**This library is simple to use, but the Xiaomi API is complex.**

For example, all notifications require a **two-step build process** and **action buttons** require special setup.

We **strongly recommend** reading the official Wiki to understand how to build your notifications correctly.

## ➡️ [Go to the Full GitHub Wiki](https://github.com/D4vidDf/HyperIsland-ToolKit/wiki)

**Key Wiki Pages:**
* **[Getting Started](https://github.com/D4vidDf/HyperIsland-ToolKit/wiki/Getting-Started):** Explains installation and the **critical** two-step build process.
* **[Handling Actions & Intents](https://github.com/D4vidDf/HyperIsland-ToolKit/wiki/Handling-Actions-&-Intents):** Explains how to make buttons work.
* **[Notification Types & Examples](https://github.com/D4vidDf/HyperIsland-ToolKit/wiki/Notification-Types-&-Examples):** A "cookbook" for timers, progress bars, etc.

---

## Installation

The library is available on **Maven Central**.

1.  Add `mavenCentral()` to your repositories in your root `settings.gradle.kts` (it's usually there by default).

2.  Add the dependency to your app-level `build.gradle.kts` file:
```gradle
dependencies {
    implementation("io.github.d4viddf:hyperisland_kit:0.3.0")
}
```
## License
Copyright 2024 D4vidDf

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUTANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
