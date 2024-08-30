import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)

}
val localProperties = Properties()
localProperties.load(rootProject.file("local.properties").inputStream())

val naverMapClientId: String = localProperties.getProperty("NAVER_MAP_CLIENT_ID") ?: ""
val naverMapApiKey: String = localProperties.getProperty("NAVER_MAP_API_KEY") ?: ""
val naverSearchClientId: String = localProperties.getProperty("NAVER_SEARCH_CLIENT_ID") ?: ""
val naverSearchApiKey: String = localProperties.getProperty("NAVER_SEARCH_API_KEY") ?: ""

android {
    namespace = "com.example.eyekeep"
    compileSdk = 34

    buildFeatures{
        buildConfig = true
    }

    viewBinding {
        enable = true
    }

    defaultConfig {
        applicationId = "com.example.eyekeep"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "NAVER_MAP_CLIENT_ID", "\"$naverMapClientId\"")
        buildConfigField("String", "NAVER_MAP_API_KEY", "\"$naverMapApiKey\"")
        buildConfigField("String", "NAVER_SEARCH_CLIENT_ID", "\"$naverSearchClientId\"")
        buildConfigField("String", "NAVER_SEARCH_API_KEY", "\"$naverSearchApiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics") // 파이어베이스 앱 분석
    implementation("com.google.firebase:firebase-auth") // 파이어베이스 인증
    implementation("com.google.firebase:firebase-firestore") // 파이어베이스 스토어
    implementation("com.google.firebase:firebase-messaging:21.1.0")
    //implementation("com.google.firebase:firebase-database")

    implementation("com.naver.maps:map-sdk:3.18.0")                         // 네이버 맵 SDK
    implementation("com.google.android.gms:play-services-location:21.0.1")  // 위치
    //implementation("com.navercorp.nid:oauth:5.9.1") // jdk 11               네이버 로그인
    implementation("com.navercorp.nid:oauth-jdk8:5.9.1") // jdk 8           네이바 로그인
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    implementation ("androidx.appcompat:appcompat:1.3.1")
    implementation ("androidx.legacy:legacy-support-core-utils:1.0.0")
    implementation ("androidx.browser:browser:1.4.0")
    implementation ("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")
    implementation ("androidx.core:core-ktx:1.3.0")
    implementation ("androidx.fragment:fragment-ktx:1.3.6")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.moshi:moshi-kotlin:1.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.2.1")
    implementation ("com.airbnb.android:lottie:3.1.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")         //retrofit 라이브러리 추가
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")   //Gson추가 JSON데이터 JAVA객체로 변환

    implementation("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    implementation ("androidx.recyclerview:recyclerview:1.3.1")

    //implementation ("com.kakao.sdk:v2-all:2.20.3") // 전체 모듈 설치, 2.11.0 버전부터 지원
   // implementation ("com.kakao.sdk:v2-user:2.20.3") // 카카오 로그인 API 모듈
   // implementation ("com.kakao.sdk:v2-share:2.20.3") // 카카오톡 공유 API 모듈
  //  implementation ("com.kakao.sdk:v2-talk:2.20.3") // 카카오톡 채널, 카카오톡 소셜, 카카오톡 메시지 API 모듈
  //  implementation ("com.kakao.sdk:v2-friend:2.20.3") // 피커 API 모듈
   // implementation ("com.kakao.sdk:v2-navi:2.20.3") // 카카오내비 API 모듈
  //  implementation ("com.kakao.sdk:v2-cert:2.20.3") // 카카오톡 인증 서비스 API 모듈
   // implementation(platform("com.squareup.okhttp3:okhttp:4.11.0"))          //OKhttp 정의 및 버전 지정
   // implementation("com.squareup.okhttp3:okhttp:")                          //OKhttp 기본라이브러리 포함
   // implementation("com.squareup.okhttp3:logging-interceptor:")            //OKhttp 로깅 인터쉡터 포함

}