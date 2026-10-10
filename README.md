# Immersive Machinery

A bunch of rustic machinery to transport, mine, and automate things, while staying close to the vanilla style.

[![Crowdin](https://badges.crowdin.net/immersive-collection/localized.svg)](https://crowdin.com/project/immersive-collection)

Hosted on
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/immersive-machinery) and
[Modrinth](https://modrinth.com/mod/immersive-machinery)

## Development

Minecraft 26.2 requires JDK 25. Build both loaders with `./gradlew build`.
The release JARs are written to `fabric/build/libs` and `neoforge/build/libs`.
Immersive Aircraft 1.6.0 for the matching loader is required and is included in the development runtime.

Shared code and assets live in `common`. Fabric uses unobfuscated Fabric Loom, and NeoForge uses
ModDevGradle. Both loader modules compile the shared sources directly, following the
[MultiLoader Template](https://github.com/jaredlll08/MultiLoader-Template/tree/26.2) source-sharing pattern.

Run clients with `./gradlew :fabric:runClient` or `./gradlew :neoforge:runClient`.
Run dedicated servers with `./gradlew :fabric:runServer` or `./gradlew :neoforge:runServer`.
Each run uses its own `run/client` or `run/server` directory inside the loader module.
