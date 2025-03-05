plugins {
    val kotlinVersion = "1.8.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("com.github.gmazzo.buildconfig") version "3.1.0"
    id("net.mamoe.mirai-console") version "2.16.0"
}

group = "com.jiugong"
version = "0.1.0"

dependencies {
    compileOnly("net.mamoe:mirai-core:2.16.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.genai:google-genai:0.1.0")
}

repositories {
//    if (System.getenv("CI")?.toBoolean() != true) {
//        maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
//    }
//    google()
    mavenCentral()
}

mirai {
    coreVersion = "2.16.0" // mirai-core version

    publishing {
        repo = "mirai"
        packageName = "mirai-console-example-plugin"
        override = true
    }
}
