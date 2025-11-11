# **HyperIsland Kit 🏝️**
<img alt="Version 0.1.1" src="https://img.shields.io/badge/version-0.1.1-blue"/>
A simple, fluent Kotlin builder library for creating notifications on Xiaomi's HyperIsland. This library abstracts away the complex JSON and Bundle-linking, allowing you to build HyperIsland notifications with a few lines of Kotlin.

## **Why Use HyperIsland Kit?**

* **Fluent Builder:** No more manual JSON string building.  
* **Type-Safe:** Uses Kotlin data classes to ensure your notification structure is valid.  
* **Auto-Bundling:** Automatically links your PendingIntent actions and Icon bitmaps to the correct keys in the JSON payload.  
* **Kotlin-First:** Written in pure Kotlin with a focus on simplicity.  
* **Well-Documented:** KDoc explains every builder method and its purpose.

## **Setup**

**Note:** The library is not yet published. Once it is, you can follow these steps.

1. Add the mavenCentral() repository to your root settings.gradle.kts (it's usually there by default).  
2. Add the dependency to your app's build.gradle.kts file:
```kotlin
dependencies {  
     
    implementation("io.github.d4viddf.hyperisland-kit:hyperisland_kit:0.1.1")  
}
```
## **How to Use**

### **1\. Check for Support**

First, always check if the device supports HyperIsland. 
```kotlin
import com.d4viddf.hyperisland\_kit.HyperIslandNotification

if (HyperIslandNotification.isSupported(context)) {  
    // The device is a Xiaomi device with HyperIsland support  
    // ... proceed to build the notification  
}
```
### **2\. Define Keys and Resources**

Define unique string keys for any pictures or actions you want to use.  
```kotlin
// Define unique keys  
const val PIC\_KEY\_APP\_OPEN \= "pic.app.open"

// Create your standard Android PendingIntent  
val openAppIntent \= PendingIntent.getActivity(  
    context, 0, Intent(context, MainActivity::class.java),  
    PendingIntent.FLAG\_IMMUTABLE  
)

// Create a HyperPicture (this converts your vector to a bitmap)  
val appPicture \= HyperPicture(PIC\_KEY\_APP\_OPEN, context, R.drawable.ic\_my\_app\_icon)
```
### **3\. Build the HyperIsland Extras**

Use the HyperIslandNotification.Builder to create the special Bundle.  
``` kotlin
val hyperIslandExtras \= HyperIslandNotification  
    .Builder(context, "myDemoApp", "Your app is running")  
    // 1\. (Optional) For "drag-to-open"  
    .setSmallWindowTarget("com.github.d4viddf.hyperislandkit.demo.MainActivity")  
    // 2\. Define the Expanded Notification Panel (Chat or Base)  
    .setBaseInfo(  
        title \= "App Open Demo",  
        content \= "Tap or drag to open the app",  
        pictureKey \= PIC\_KEY\_APP\_OPEN  
    )  
    // 3\. Define the Expanded Island  
    .setBigIslandInfo(  
        ImageTextInfoLeft(  
            picInfo \= PicInfo(type \= 1, pic \= PIC\_KEY\_APP\_OPEN),  
            textInfo \= TextInfo(title \= "App Demo", content \= "Running")  
        )  
    )  
    // 4\. Define the Summary Island  
    .setSmallIslandIcon(PIC\_KEY\_APP\_OPEN)  
    // 5\. Add the pictures to the bundle  
    .addPicture(appPicture)  
    // 6\. Build the final Bundle  
    .buildExtras()
```
### **4\. Add to your Notification**

Finally, add the generated extras to your standard NotificationCompat.Builder and fire the notification.  
```kotlin
val notification \= NotificationCompat.Builder(context, YOUR\_CHANNEL\_ID)  
    .setSmallIcon(R.drawable.ic\_stat\_notification)  
    .setContentTitle("App Open Demo")  
    .setContentText("Tap or drag to open the app.")  
    // \--- This is the standard "tap-to-open" \---  
    .setContentIntent(openAppIntent)  
    // \--- This adds all the HyperIsland magic \---  
    .addExtras(hyperIslandExtras)  
    .build()

NotificationManagerCompat.from(context).notify(123, notification)
```
## **API Overview**

### **Main Builder Methods**

| Method | Description |
| :---- | :---- |
| **.setChatInfo(...)** | Sets the expanded notification panel to the "Chat" style. |
| **.setBaseInfo(...)** | Sets the expanded notification panel to the "Base" style (an alternative to setChatInfo). |
| **.setSmallIsland(...)** | Sets the summary island to the A/B zone text/icon style. |
| **.setSmallIslandIcon(...)** | Sets the summary island to a simple icon. |
| **.setSmallIslandCircularProgress(...)** | Sets the summary island to the icon \+ circular progress style. |
| **.setBigIslandInfo(...)** | Sets the expanded island to the simple text \+ icon style. |
| **.setBigIslandCountdown(...)** | Sets the expanded island to the icon \+ countdown timer style. |
| **.setBigIslandCountUp(...)** | Sets the expanded island to the icon \+ count-up timer style. |
| **.setBigIslandProgressCircle(...)** | Sets the expanded island to the icon \+ circular progress style. |
| **.setProgressBar(...)** | Adds a linear progress bar to the bottom of the expanded panel. |
| **.setSmallWindowTarget(...)** | Enables the "drag-to-open" app feature. |

### **Helper Objects**

* HyperAction(key, ...): Create this for any button you want to add.  
* HyperPicture(key, ...): Create this for any icon/image you want to display.  
* **Remember:** You must call .addAction(myAction) and .addPicture(myPicture) for every action and picture you use.

## **License**

This project is licensed under the Apache 2.0 License \- see the LICENSE file for details.
