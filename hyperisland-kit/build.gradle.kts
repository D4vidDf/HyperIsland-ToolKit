import com.vanniktech.maven.publish.SonatypeHost
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    id("com.vanniktech.maven.publish") version "0.28.0" // Use a recent version

}
android {

    namespace = "io.github.d4viddf.hyperisland_kit"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {

        release {

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

        }


    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

mavenPublishing {


    coordinates("io.github.d4viddf", "hyperisland_kit", "0.3.0")

    pom {
        name = "HyperIsland ToolKit"
        description = "A simple Kotlin library for creating notifications on Xiaomi's HyperIsland. Abstracts away the complex JSON and Bundle-linking, allowing you to build HyperIsland notifications with a few lines of Kotlin."
        inceptionYear = "2025"
        url = "https://github.com/D4vidDf/HyperIsland-ToolKit"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "d4viddf"
                name = "D4vidDF"
                email="d4viddf@d4viddf.com"
                organization = "D4vidDF"
                organizationUrl = "https://d4viddf.com"
                url = "https://d4viddf.com"
            }
        }
        scm {
            url = "https://github.com/D4vidDf/HyperIsland-ToolKit"
            connection = "scm:git:git://github.com/D4vidDf/HyperIsland-ToolKit.git"
            developerConnection = "scm:git:ssh://git@github.com/D4vidDf/HyperIsland-ToolKit.git"
        }
    }
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.kotlinx.serialization.json)
}