# HyperIsland ToolKit 🏝️
[![Maven Central](https://img.shields.io/maven-central/v/io.github.d4viddf/hyperisland_kit)](https://central.sonatype.com/artifact/io.github.d4viddf/hyperisland_kit)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A powerful, type-safe Kotlin library for integrating **Xiaomi HyperOS Dynamic Island** notifications (Focus Notifications) into your Android apps.

This library abstracts away the undocumented JSON payloads and complex Bundle logic, providing a clean **Kotlin DSL** to build rich, pixel-perfect system notifications.

---

## 📚 Documentation

The full documentation, guides, and component references are available on our new website:

## ➡️ [Read the Documentation](https://d4viddf.github.io/HyperIsland-ToolKit/)

**Quick Links:**
* 🚀 **[Getting Started](https://hyperisland.d4viddf.com/docs/getting-started/)** - Installation & "Hello World".
* 🛠 **[The Builder](https://hyperisland.d4viddf.com/docs/builder/)** - How to generate the payloads.
* 🧩 **[Components](https://hyperisland.d4viddf.com/docs/components/)** - Templates, Actions, and Progress Bars.
* 🏝 **[Dynamic Island](https://hyperisland.d4viddf.com/docs/components/island/configuration/)** - Customizing the pill shape and behavior.

---

## Features
* **Type-Safe DSL:** No more manual JSON string concatenation.
* **Smart Defaults:** Automatically handles system prefixes like `miui.focus.pic_`.
* **20+ Templates:** Support for Chat, Media, Timer, Upload, Taxi, and more.
* **Native Integration:** Uses standard Android Notification APIs under the hood.

## Installation

Add the dependency to your app-level `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("io.github.d4viddf:hyperisland_kit:0.4.0")
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
