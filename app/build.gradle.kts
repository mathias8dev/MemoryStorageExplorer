import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.kotlin.parcelize)
}

fun computeVersionCode(versionMajor: String, versionMinor: String, versionPatch: String): Int = run {
    versionMajor.toInt() * 100000 +
            versionMinor.toInt() * 1000 +
            versionPatch.toInt() * 10
}


fun computeVersionName(versionMajor: String, versionMinor: String, versionPatch: String): String = run {
    "$versionMajor.$versionMinor.$versionPatch"
}


fun computeNotProdVersionName(versionMajor: String, versionMinor: String, versionPatch: String): String = run {
    "${computeVersionName(versionMajor, versionMinor, versionPatch)}-${localDatetime()}"
}


fun localDatetime(): String = run {
    val formatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmm", Locale.FRANCE)
    formatter.format(LocalDateTime.now())
}

android {
    namespace = "com.mathias8dev.memoriesstoragexplorer"
    compileSdk = 35

    val versionMajor: String by project
    val versionMinor: String by project
    val versionPatch: String by project

    defaultConfig {
        applicationId = "com.mathias8dev.memoriesstorageexplorer"
        minSdk = 24
        targetSdk = 35
        versionCode = computeVersionCode(versionMajor, versionMinor, versionPatch)
        versionName = computeVersionName(versionMajor, versionMinor, versionPatch)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    applicationVariants.all {
        addJavaSourceFoldersToModel(
            File(layout.buildDirectory.get().asFile, "generated/ksp/$name/kotlin")
        )
    }
}

dependencies {


    implementation(libs.jsoup)

    implementation(files("libs/sun-common-server.jar"))
    implementation(files("libs/http-2.2.1.jar"))

    implementation(libs.lottie.compose)

    // Secured shared preferences
    implementation(libs.androidx.security.crypto)

    // gson
    implementation(libs.google.code.gson)
    // Datastore
    implementation(libs.androidx.datastore)

    // pdf viewer
    implementation(libs.pdf.viewer)

    // Scrollbar provider
    implementation(libs.lazycolumnscrollbar)

    // ExoPlayer
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.ui)

    implementation(libs.mimemagic.android)

    implementation(libs.permissionhelper)

    implementation(libs.androidx.navigation.compose)


    // Timber
    implementation(libs.timber)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // State Management (collectAsStateWithLifecycle) and others
    implementation(libs.androidx.lifecycle.runtime.compose)


    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // ComposeDestination
    implementation(libs.animations.core)
    ksp(libs.ksp)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    implementation(libs.koin.androidx.compose)
    ksp(libs.koin.ksp.compiler)

    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.coil.gif)
    implementation(libs.coil.video)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // material icons extended
    implementation(libs.androidx.material.icons.extended)

    // Core library desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}