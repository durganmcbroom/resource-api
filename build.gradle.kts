plugins {
    kotlin("multiplatform") version "1.9.21"
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.9.10"
}

group = "com.durganmcbroom"
version = "1.1.7-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()

    jvm {
        jvmToolchain(8)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()

            testLogging {
                showStandardStreams = true
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {}
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {}
        }
        val jvmTest by getting {}
    }
}

tasks.register("publishAll") {
    dependsOn(allprojects.map { it.tasks.getByName("publish") })
}

tasks.register("publishAllLocally") {
    dependsOn(allprojects.map { it.tasks.getByName("publishToMavenLocal") })
}

allprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")

    val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

    tasks.register<Jar>("javadocJar") {
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaHtml.outputDirectory)
    }
    publishing {
        repositories {
            maven {
                name = "extframework-repo"
                url = uri("https://maven.extframework.dev/snapshots")

                credentials {
                    val user = project.findProperty("maven.user") as String?
                    if (user == null) System.err.println("Couldnt find maven user")
                    else println("Found maven user: '$user'")
                    username = user

                    val userKey = project.findProperty("maven.key") as String?
                    if (user == null) System.err.println("Couldnt find maven key")
                    else println("Found maven key")
                    password = userKey
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        artifact(tasks["javadocJar"])

        pom {
            name.set("resource-api")

            packaging = "jar"

            developers {
                developer {
                    id.set("durganmcbroom")
                    name.set("Durgan McBroom")
                }
            }

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/durganmcbroom/resource-api")
                developerConnection.set("scm:git:ssh://github.com:durganmcbroom/resource-api")
                url.set("https://github.com/durganmcbroom/resource-api")
            }
        }
    }
}