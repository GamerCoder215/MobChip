import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val versions = listOf(
    "1_13_R1",
    "1_13_R2",
    "1_14_R1",
    "1_15_R1",
    "1_16_R1",
    "1_16_R2",
    "1_16_R3",
    "1_17_R1",
    "1_18_R1",
    "1_18_R2",
    "1_19_R1",
    "1_19_R2",
    "1_19_R3",
    "1_20_R1",
    "1_20_R2",
    "1_20_R3"
)

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.13-R0.1-SNAPSHOT") {
        version {
            strictly("1.13-R0.1-SNAPSHOT")
        }
    }

    // API

    api(project(":mobchip-base"))
    api(project(":mobchip-abstraction"))

    versions.forEach { api(project(":mobchip-$it")) }
}

java {
    withJavadocJar()
}

sourceSets["main"].allJava.srcDir("src/main/javadoc")

tasks {
    compileJava {
        versions.subList(versions.indexOf("1_18_R1"), versions.size).forEach { dependsOn(project(":mobchip-$it").tasks["assemble"]) }
    }

    javadoc {
        enabled = true

        options {
            require(this is StandardJavadocDocletOptions)

            links("https://hub.spigotmc.org/javadocs/spigot/")
            links("https://javadoc.io/doc/org.jetbrains/annotations-java5/23.0.0/")
        }
    }

    register("sourcesJar", Jar::class.java) {
        archiveClassifier.set("sources")

        val sources = listOf(
            sourceSets["main"].allSource,
            project(":mobchip-base").sourceSets["main"].allSource
        )

        from(sources)
    }

    jar.configure {
        artifacts {
            add("archives", getByName<Jar>("sourcesJar"))
        }
    }

    withType<ShadowJar> {
        dependsOn("sourcesJar", "javadocJar")
    }
}