<img src="resources/logo.svg" alt="Logo" align="right" width="150">

# Horizon

**Horizon is a Purpur fork with many useful optimizations, configurable vanilla features, and more API supports**

## Features
- **Fully compatible** with Bukkit, Spigot and Paper plugins
- **Mod Protocols** support
- **Linear region file format** support (by [LinearPaper](https://github.com/StupidCraft/LinearPaper))
- **Secure Seed** support
- **Replay API** support (by [Leaves](https://github.com/LeavesMC/Leaves))
- **and more in future**

## API
### [Javadoc](https://repo.timelesswaffle.su/javadoc/snapshots/dev/gideonwhite1029/horizon/horizon-api/1.21.10-R0.1-SNAPSHOT)
### Dependency Information
Maven
```xml
<repository>
    <id>horizon</id>
    <url>https://repo.timelesswaffle.su/snapshots</url>
</repository>
```
```xml
<dependency>
    <groupId>dev.gideonwhite1029.horizon</groupId>
    <artifactId>horizon-api</artifactId>
    <version>1.21.10-R0.1-SNAPSHOT</version>
</dependency>
```

Gradle
```kotlin
repositories {
    maven("https://repo.timelesswaffle.su/snapshots")
}
```
```kotlin
dependencies {
    compileOnly("dev.gideonwhite1029.horizon:horizon-api:1.21.10-R0.1-SNAPSHOT")
}
```

## Build
To build a paperclip jar, you need to run the following command. You can find the jar in build/libs(Note: JDK17 or JDK21 is needed)

 ```shell
 ./gradlew applyAllPatches && ./gradlew createMojmapPaperclipJar
```

## About Issue
When you meet any problems, just ask us, we will do our best to solve it, but remember to state your problem clear and provide enough logs etc.

## Contributing
This readme will eventually contain instructions regarding the patch system. For now, visit Horizon's [CONTRIBUTING.md](https://github.com/GideonWhite1029/Horizon/blob/ver/1.21.4/CONTRIBUTING.md).

## Thank you
Thanks to these projects below. Horizon just mix some of their patches together.

- [Leaves](https://github.com/LeavesMC/Leaves)
- [Leaf](https://github.com/Winds-Studio/Leaf)
- [Gale](https://github.com/GaleMC/Gale)
- [Purpur](https://github.com/PurpurMC/Purpur)
- [Pufferfish](https://github.com/pufferfish-gg/Pufferfish)
- [LinearPaper](https://github.com/StupidCraft/LinearPaper)
- [Canvas](https://github.com/CraftCanvasMC/Canvas)