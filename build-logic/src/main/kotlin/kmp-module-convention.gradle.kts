@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    // js()

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting

        val jvmMain by getting

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val macosX64Main by getting
        val macosArm64Main by getting

//        val iosMain by creating {
//            dependsOn(commonMain)
//            iosX64Main.dependsOn(this)
//            iosArm64Main.dependsOn(this)
//            iosSimulatorArm64Main.dependsOn(this)
//        }

        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val macosX64Test by getting
        val macosArm64Test by getting

//        val iosTest by creating {
//            dependsOn(commonTest)
//            iosX64Test.dependsOn(this)
//            iosArm64Test.dependsOn(this)
//            iosSimulatorArm64Test.dependsOn(this)
//        }
//
//        val appleMain by creating {
//            dependsOn(commonMain)
//            iosArm64Main.dependsOn(this)
//            iosX64Main.dependsOn(this)
//            iosSimulatorArm64Main.dependsOn(this)
//            macosX64Main.dependsOn(this)
//            macosArm64Main.dependsOn(this)
//        }
//
//        val appleTest by creating {
//            dependsOn(commonTest)
//            iosArm64Test.dependsOn(this)
//            iosX64Test.dependsOn(this)
//            iosSimulatorArm64Test.dependsOn(this)
//            macosX64Test.dependsOn(this)
//            macosArm64Test.dependsOn(this)
//        }
    }
}