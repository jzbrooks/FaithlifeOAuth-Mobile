import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform") version "1.6.10"
    id("org.jetbrains.changelog") version "1.3.1"
    id("com.vanniktech.maven.publish") version "0.18.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("com.android.library")
}

group = "com.faithlife"
version = "0.0.4"

repositories {
    google()
    mavenCentral()
}

kotlin {
    android {
        publishLibraryVariants("release")
    }

    val frameworkBaseName = "FaithlifeOAuth"
    val xcFramework = XCFramework(frameworkBaseName)
    ios {
        binaries.framework {
            baseName = frameworkBaseName
            xcFramework.add(this)
        }
        tasks.build.dependsOn("assemble" + frameworkBaseName + "XCFramework")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.0.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("com.willowtreeapps.assertk:assertk:0.25")
            }
        }

        val androidMain by getting
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13.1")
            }
        }

        val iosMain by getting
        val iosTest by getting
    }
}

android {
    compileSdk = 31
    sourceSets.getByName("main").manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 31
    }

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

ktlint {
    version.set("0.43.0")
}

tasks.withType<Test> {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
    }

    addTestListener(object : TestListener {
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent == null) {
                val output =
                    "|  Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)  |"
                val border = "-".repeat(output.length)
                println(
                    """
                        $border
                        $output
                        $border
                    """.trimIndent()
                )
            }
        }

        override fun afterTest(testDescriptor: TestDescriptor?, result: TestResult?) {}
        override fun beforeTest(testDescriptor: TestDescriptor?) {}
        override fun beforeSuite(suite: TestDescriptor?) {}
    })
}
