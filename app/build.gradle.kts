plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.computerwork"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.computerwork"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
    testImplementation ("org.mockito:mockito-inline:5.2.0")
    testImplementation ("org.mockito:mockito-core:5.2.0")
    testImplementation ("androidx.appcompat:appcompat:1.6.1")
    testImplementation ("junit:junit:4.13.2'")
    testImplementation ("org.mockito:mockito-core:5.+")
    testImplementation ("net.bytebuddy:byte-buddy:1.17.5") // или новейшая
    testImplementation ("net.bytebuddy:byte-buddy-agent:1.17.5")
    testImplementation ("org.robolectric:robolectric:4.10.3")
    testImplementation ("androidx.test:core:1.5.0")
    testImplementation ("androidx.test.ext:junit:1.1.5")
    testImplementation ("androidx.test.espresso:espresso-core:3.5.1")

    testImplementation ("org.mockito:mockito-core:4.+")  // Последняя версия Mockito
    testImplementation ("org.mockito:mockito-inline:4.+")


    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}