import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import java.io.File
import org.gradle.api.Project

const val baseVersionName = "2.5.3"
val Project.verName: String get() = "${baseVersionName}${versionNameSuffix}.${exec("git rev-parse --short=7 HEAD")}"
val Project.verCode: Int get() = exec("git rev-list --count HEAD").toInt()
val Project.isDevVersion: Boolean get() = exec("git tag -l $baseVersionName").isEmpty()
val Project.versionNameSuffix: String get() = if (isDevVersion) ".dev" else ""

fun Project.setupLibraryModule(block: LibraryExtension.() -> Unit = {}) {
  setupBaseModule(block)
}

fun Project.setupAppModule(block: BaseAppModuleExtension.() -> Unit = {}) {
  setupBaseModule<BaseAppModuleExtension> {
    defaultConfig {
      versionCode = verCode
      versionName = verName
      androidResources {
        ignoreAssetsPatterns += "!PublicSuffixDatabase.list" // OkHttp5
        generateLocaleConfig = true
        localeFilters += mutableSetOf(
          "en",
          "ar-rSA",
          "de-rDE",
          "in-rID",
          "iw-rIL",
          "ja-rJP",
          "pt-rBR",
          "ru-rRU",
          "tr-rTR",
          "uk-rUA",
          "vi-rVN",
          "zh-rCN",
          "zh-rTW",
          "zh-rHK",
        )
      }
    }
    val releaseSigning = signingConfigs.create("release") {
      storeFile = rootProject.file("release.jks")
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = System.getenv("KEY_ALIAS")
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    buildTypes {
      debug {
        applicationIdSuffix = ".debug"
      }
      release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard-rules.pro"
        )
      }
      all {
        signingConfig = releaseSigning
        buildConfigField("Boolean", "IS_DEV_VERSION", isDevVersion.toString())
        //buildConfigField("String", "BUILD_TIME", "\"" + Instant.now().toString() + "\"")
      }
    }

    block()
  }
}

private inline fun <reified T : BaseExtension> Project.setupBaseModule(crossinline block: T.() -> Unit = {}) {
  extensions.configure<BaseExtension>("android") {
    (this as? CommonExtension<*, *, *, *, *, *>)?.apply {
      compileSdk {
        version = release(36) {
          minorApiLevel = 1
        }
      }
    } ?: {
      compileSdkVersion(36)
    }
    defaultConfig {
      minSdk = 24
      targetSdk = 36
    }
    sourceSets.configureEach {
      java.srcDirs("src/$name/kotlin")
    }
    (this as T).block()
  }
}

fun Project.exec(command: String): String = providers.exec {
  commandLine(command.split(" "))
}.standardOutput.asText.get().trim()
