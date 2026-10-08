import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "cat.descobreix"
    compileSdk = 36

    defaultConfig {
        applicationId = "cat.descobreix"
        minSdk = 26
        targetSdk = 36
        // A la CI, el número de la compilació, que sempre creix: Android només accepta una actualització
        // si el versionCode és més gran que el de l'app instal·lada (docs/obtainium.md).
        val compilacio = System.getenv("VERSIO_CODI")?.toIntOrNull()
        versionCode = compilacio ?: 1
        versionName = if (compilacio != null) "1.0.$compilacio" else "1.0"
        testInstrumentationRunner = "cat.descobreix.HiltTestRunner"

        // Projecte de Supabase (docs/requisits.md, secció 9). Només la URL i la clau pública
        // (anon), llegides de local.properties, que no es puja al repositori.
        val local = Properties().apply {
            rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
        }
        // A la CI que publica l'app, surten dels secrets del repositori.
        val url = local.getProperty("supabase.url") ?: System.getenv("SUPABASE_URL") ?: ""
        val anonKey = local.getProperty("supabase.anonKey") ?: System.getenv("SUPABASE_ANON_KEY") ?: ""
        buildConfigField("String", "SUPABASE_URL", "\"$url\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$anonKey\"")
    }

    // La clau de signatura de les versions publicades (docs/obtainium.md). Mai al repositori: la CI la treu
    // dels secrets i la deixa en un fitxer temporal. Sempre la mateixa, perquè cada APK s'instal·li sobre l'anterior.
    val fitxerClau = System.getenv("SIGNATURA_FITXER")
    signingConfigs {
        if (fitxerClau != null) {
            create("publicacio") {
                storeFile = file(fitxerClau)
                storePassword = System.getenv("SIGNATURA_CONTRASENYA")
                keyAlias = System.getenv("SIGNATURA_ALIES")
                keyPassword = System.getenv("SIGNATURA_CONTRASENYA_CLAU") ?: System.getenv("SIGNATURA_CONTRASENYA")
            }
        }
    }

    buildTypes {
        release {
            if (fitxerClau != null) signingConfig = signingConfigs.getByName("publicacio")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        // limits.bin es llegeix a trossos: sense comprimir, es pot saltar directament a cada municipi.
        noCompress += "bin"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        textReport = true
        textOutput = file("build/reports/lint-results.txt")
        // Les versions de les dependències s'actualitzen a mà.
        disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("cat.descobreix:joc:1.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.camera.core)
    implementation(libs.tesseract4android)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.androidx.work.runtime)
    implementation(libs.ktor.client.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(kotlin("test-junit"))

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
