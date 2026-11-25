plugins {
    java
    id("com.gradleup.shadow") version "9.3.0"
}

group = "ru.javaroot.elo"
version = providers.gradleProperty("pluginVersion").get()

val targetJava = 21

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJava))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("com.zaxxer:HikariCP:6.2.1") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.1") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.xerial:sqlite-jdbc:3.49.1.0")
    testImplementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(targetJava)
    }

    compileTestJava {
        options.encoding = "UTF-8"
        options.release.set(targetJava)
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        filteringCharset = "UTF-8"
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("com.zaxxer.hikari", "ru.javaroot.elo.libs.hikari")
        relocate("org.mariadb.jdbc", "ru.javaroot.elo.libs.mariadb")
        minimize {
            exclude(dependency("org.mariadb.jdbc:mariadb-java-client:.*"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
