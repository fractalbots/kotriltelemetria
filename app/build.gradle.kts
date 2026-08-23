plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    dependencies {

        // ── Base ──
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)

        // ── Compose ──
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.graphics)
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.material.icons.extended)

        // ── Navegación y ViewModel ──
        implementation(libs.androidx.navigation.compose)
        implementation(libs.androidx.lifecycle.viewmodel.compose)
        implementation(libs.androidx.lifecycle.runtime.compose)

        // ── Room ──
        implementation(libs.androidx.room.runtime)
        implementation(libs.androidx.room.ktx)
        ksp(libs.androidx.room.compiler)

        // ── DataStore ──
        implementation(libs.androidx.datastore.preferences)

        // ── Retrofit + Moshi ──
        implementation(libs.retrofit.core)
        implementation(libs.retrofit.moshi)
        implementation(libs.moshi.kotlin)
        implementation(libs.okhttp.logging)

        // ── Coil ──
        implementation(libs.coil.compose)
        implementation(libs.coil.network)

        // ── CameraX + ML Kit ──
        implementation(libs.androidx.camera.core)
        implementation(libs.androidx.camera.camera2)
        implementation(libs.androidx.camera.lifecycle)
        implementation(libs.androidx.camera.view)
        implementation(libs.mlkit.barcode.scanning)

        // ── GPS ──
        implementation(libs.play.services.location)

        // ── Permisos ──
        implementation(libs.accompanist.permissions)

        // ── Tests ──
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.compose.ui.test.junit4)
        debugImplementation(libs.androidx.compose.ui.tooling)
        debugImplementation(libs.androidx.compose.ui.test.manifest)
    }
}
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}