import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.google.services)
  alias(libs.plugins.firebase.crashlytics.gradle)
  alias(libs.plugins.play.publisher)
}

android {
  namespace = "info.bvlion.watchify"
  compileSdk = 35

  defaultConfig {
    applicationId = "info.bvlion.watchify"
    minSdk = 31
    targetSdk = 35
    versionCode = 3
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  val localProperties = Properties()
  val localPropertiesFile = rootProject.file("local.properties")
  if (localPropertiesFile.exists()) {
      localPropertiesFile.inputStream().use {
          localProperties.load(it)
      }
  }

  defaultConfig {
    buildConfigField("String", "RT_DB_URL", "\"${localProperties.getProperty("RT_DB_URL")}\"")
    buildConfigField("String", "CONTACT_URL", "\"${localProperties.getProperty("CONTACT_URL")}\"")
  }

  signingConfigs {
    create("release") {
      storeFile = file("../release.jks")
      storePassword = localProperties.getProperty("KEYSTORE_PASSWORD")
      keyAlias = localProperties.getProperty("KEYSTORE_ALIAS")
      keyPassword = localProperties.getProperty("KEYSTORE_PASSWORD")
    }
  }

  buildTypes {
    debug {
      isDebuggable = true
      versionNameSuffix = "-debug"
    }
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      signingConfig = signingConfigs.getByName("release")
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      ndk {
        debugSymbolLevel = "FULL"
      }
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.15"
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  implementation(project(":AppInfoManager"))
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.work)
  implementation(libs.androidx.fragment)
  implementation(libs.androidx.splashscreen)
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.crashlytics)
  implementation(libs.firebase.messaging)
  implementation(libs.firebase.database.ktx)
  implementation(libs.firebase.firestore.ktx)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}

play {
  track.set("alpha")
  serviceAccountCredentials.set(file("../google-play-service.json"))
}