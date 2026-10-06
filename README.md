# HyperIsland ToolKit 🏝️
[![Maven Central](https://img.shields.io/maven-central/v/io.github.d4viddf/hyperisland_kit)](https://central.sonatype.com/artifact/io.github.d4viddf/hyperisland_kit)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A powerful, type-safe Kotlin library for integrating **Xiaomi HyperOS HyperIsland** notifications into your Android applications.

This library abstracts away undocumented JSON payloads, complex Bundle-linking, and image resource prefixes, providing a clean **Kotlin DSL** to build rich, pixel-perfect system notifications with just a few lines of code.

---

## 🚀 Quick Start (Version 0.4.5)

Add the dependency to your app-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.d4viddf:hyperisland_kit:0.4.5")
}
```

### Kotlin DSL Example

```kotlin
val notification = HyperIslandNotification.Builder(context)
    .setSmallIcon(R.drawable.ic_notification)
    .setContentTitle("Flight Update")
    .setContentText("Boarding at Gate B12 • Seat 4A")
    .setIslandInfo(
        title = "Flight MI-808",
        subtitle = "Gate B12",
        type = IslandType.AIRLINE
    )
    .build()

notificationManager.notify(NOTIFICATION_ID, notification)
```

---

## 📲 Demo App & Real-Time Inspector

The repository includes a complete **Expressive Material 3 Demo & Live Inspection App** (`:demo`) to test template notifications, inspect HyperOS payloads, and diagnose device compatibility.

### Demo App Highlights:
* 🎨 **Material 3 Expressive Design**: Modern squircle surface containers, adaptive navigation (`NavigationRail` for tablets/foldables/landscape & bottom `NavigationBar` for phones), and high-contrast light/dark mode styling.
* 🔍 **Live Stream Inspector**: Grouped notification update streams (`Step 1 → Step 2 → Step 3`) tracking live state changes, title/content updates, and system attributes.
* 🖼️ **Interactive Asset Viewer**: Full-screen viewer for extracted notification icons and bitmaps featuring:
  * **Pinch-to-zoom & pan** (up to 6x scale) with quick zoom reset (`1:1`).
  * **Background contrast toggles** (*Dark*, *Light*, and *Checkerboard Grid*) for transparent PNGs and black/white icons.
  * **Gallery Export**: Save full-resolution notification bitmaps directly to `Pictures/HyperIsland`.
* 📄 **PDF & Markdown Report Exporter**: Share complete notification stream reports as formatted PDF or Markdown (`.md`) files containing system profile info, action button tables, visual asset metadata, and full, untruncated HyperIsland JSON payloads.
* 📱 **Commercial Device Diagnostics**: Displays official commercial marketing phone names (e.g. *Xiaomi 14 Pro*), model codes, codenames, HyperOS version, and Xiaomi HyperIsland compatibility status.
* ⚡ **Listener Service Health & One-Tap Rebind**: Real-time listener connection monitoring (`Active` vs `Inactive`) with a one-tap **Restart Service** button to rebind the listener service directly if unbound by the OS.

---

## 📚 Documentation

Complete documentation, design guides, and API component references are available on our official site:

## ➡️ [Read the Full Documentation](https://d4viddf.github.io/HyperIsland-ToolKit/)

**Quick Links:**
* 🚀 **[Getting Started Guide](https://hyperisland.d4viddf.com/docs/getting-started/)** - Installation, permissions, and setup.
* 🛠 **[Payload Builder](https://hyperisland.d4viddf.com/docs/builder/)** - Building custom HyperIsland parameters.
* 🧩 **[Template Catalog](https://hyperisland.d4viddf.com/docs/components/)** - Taxi, Airline, Media, Timer, Payment, and IoT templates.
* 🏝 **[Dynamic Island Config](https://hyperisland.d4viddf.com/docs/components/island/configuration/)** - Customizing pill shapes, icons, and progress bars.

---

## ✨ Key Features
* **Type-Safe Kotlin DSL:** Clean builder syntax with zero manual JSON concatenation.
* **Automatic Resource Management:** Handles `miui.focus.pic_` prefixes and bundle mapping automatically.
* **23+ Official Templates:** Full support for Weather, Music, Taxi, Payment, Progress Timers, and Custom RemoteViews.
* **Backward Compatible:** Fallbacks cleanly to standard Android system notifications on non-Xiaomi devices.

---

## 📄 License

Copyright 2025 D4vidDf

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
