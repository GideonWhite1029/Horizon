import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java // TODO java launcher tasks
    id("io.papermc.paperweight.patcher") version "2.0.0-beta.16"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"
val purpurMavenPublicUrl = "https://repo.purpurmc.org/snapshots/"

paperweight {
    upstreams.register("purpur") {
        repo = github("PurpurMC", "Purpur")
        ref = providers.gradleProperty("purpurCommit")

        patchFile {
            path = "purpur-server/build.gradle.kts"
            outputFile = file("horizon-server/build.gradle.kts")
            patchFile = file("horizon-server/build.gradle.kts.patch")
        }
        patchFile {
            path = "purpur-api/build.gradle.kts"
            outputFile = file("horizon-api/build.gradle.kts")
            patchFile = file("horizon-api/build.gradle.kts.patch")
        }
        patchRepo("paperApi") {
            upstreamPath = "paper-api"
            patchesDir = file("horizon-api/paper-patches")
            outputDir = file("paper-api")
        }
        patchDir("purpurApi") {
            upstreamPath = "purpur-api"
            excludes = listOf("build.gradle.kts", "build.gradle.kts.patch", "paper-patches")
            patchesDir = file("horizon-api/purpur-patches")
            outputDir = file("purpur-api")
        }
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    dependencies {
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
        options.isFork = true
        options.compilerArgs.addAll(listOf("-Xlint:-deprecation", "-Xlint:-removal"))
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test> {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }
    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
        maven(purpurMavenPublicUrl)
        maven("https://jitpack.io")
        maven("https://repo.timelesswaffle.su/snapshots")
    }

    extensions.configure<PublishingExtension> {
        repositories {
            maven("https://repo.timelesswaffle.su/snapshots") {
                name = "horizon"
                credentials(PasswordCredentials::class) {
                    username = System.getenv("HORIZON_USERNAME")
                    password = System.getenv("HORIZON_PASSWORD")
                }
            }
        }
    }
}

tasks.register("printMinecraftVersion") {
    doLast {
        println(providers.gradleProperty("mcVersion").get().trim())
    }
}

tasks.register("printHorizonVersion") {
    doLast {
        println(project.version)
    }
}