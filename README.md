[version_core]: https://img.shields.io/maven-metadata/v?color=informational&label=Core&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fdev%2Fmlnr%2FBotListHandler-core%2Fmaven-metadata.xml
[version_jda]: https://img.shields.io/maven-metadata/v?color=informational&label=JDA&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fdev%2Fmlnr%2FBotListHandler-jda%2Fmaven-metadata.xml
[version_javacord]: https://img.shields.io/maven-metadata/v?color=informational&label=Javacord&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fdev%2Fmlnr%2FBotListHandler-javacord%2Fmaven-metadata.xml

# BotListHandler

This handler can be used in 3 ways:
- standalone (updating the stats yourself)
- using JDA
- using Javacord

## Getting the handler

### Replace `%VERSION_xyz%` with the latest release tag:
- core (required, bundled):     ![version_core]
- javacord: ![version_javacord]
- jda: ![version_jda]

### Modules
- core: the core module of the handler, always required and bundled with every module (however it's recommended to declare the dependency separately for independent updates)
- jda: the jda module of the handler, use this if you intend to get the data from a JDA bot
- javacord: the javacord module of the handler, use this if you intend to get the data from a Javacord bot

**Gradle**
```gradle
repositories {
  mavenCentral()
}

dependencies {
  // required (bundled with every module, see the Modules section)
  implementation group: 'dev.mlnr', name: 'BotListHandler-core', version: '%VERSION_core%'
  
  // optional
  implementation group: 'dev.mlnr', name: 'BotListHandler-jda', version: '%VERSION_jda%'
  implementation group: 'dev.mlnr', name: 'BotListHandler-javacord', version: '%VERSION_javacord%'
}
```

**Maven**
```xml
<!--required (bundled with every module, see the Modules section)-->
<dependencies>
    <dependency>
        <groupId>dev.mlnr</groupId>
        <artifactId>BotListHandler-core</artifactId>
        <version>%VERSION_core%</version>
    </dependency>
<!--optional-->
    <dependency>
        <groupId>dev.mlnr</groupId>
        <artifactId>BotListHandler-jda</artifactId>
        <version>%VERSION_jda%</version>
    </dependency>
    <dependency>
        <groupId>dev.mlnr</groupId>
        <artifactId>BotListHandler-javacord</artifactId>
        <version>%VERSION_javacord%</version>
    </dependency>
</dependencies>
```

## Initialization

Using the `addBotList` method:
```java
BotListHandler botListHandler = new BLHBuilder(botId).addBotList(BotList.TOP_GG, "top_gg_token")
  .addBotList(BotList.DBOATS, "dboats_token")
  .build();
```
Using the constructor:
```java
Map<BotList, String> botLists = new EnumMap<>(BotList.class);
botLists.put(BotList.DBL, "dbl_token");
botLists.put(BotList.DEL, "del_token");

BotListHandler botListHandler = new BLHBuilder(botLists, botId).build();
```
Using the `setBotLists` method:
```java
Map<BotList, String> botLists = new EnumMap<>(BotList.class);
botLists.put(BotList.BOTLIST_SPACE, "botlist_space_token");
botLists.put(BotList.DBOTS_GG, "dbots_gg_token");

BotListHandler botListHandler = new BLHBuilder(botId).setBotLists(botLists).build();
```

## Implementation

There are 3 ways to use BotListHandler:

### Standalone
```java
botListHandler.updateAllStats(botId, serverCount);
```

### Event based (recommended)

```java
// JDA
JDA jda = JDABuilder.create("token", intents)
  .addEventListeners(new BLHJDAEventListener(botListHandler))
  .build();
  
jda.awaitReady(); // optional, but if you want to update the stats after a ReadyEvent, it's required

// Javacord
new DiscordApiBuilder().setToken(token)
        .addListener(new BLHJavacordListener(botListHandler))
        .login();
```

### Automatic stats posting
```java
// JDA
JDA jda = JDABuilder.create("token", intents)
  .build();
  
jda.awaitReady(); // optional

BotListHandler botListHandler = new BLHBuilder(new BLHJDAUpdater(jda), botLists)
  .setAutoPostDelay(20, TimeUnit.SECONDS).build();

// Javacord - async approach. call join() after login() to block
new DiscordApiBuilder().setToken(token)
        .login()
        .thenAccept(discordApi -> {
            new BLHBuilder(new BLHJavacordUpdater(discordApi), botLists)
                    .setAutoPostDelay(3, TimeUnit.MINUTES).build();
        });
```

### You can store the `BotListHandler` instance to add bot lists or hotswap invalid tokens at runtime.

## Currently supported bot lists

[botlist.space](https://botlist.space)

[botsfordiscord.com](https://botsfordiscord.com)

[botsondiscord.xyz](https://botsondiscord.xyz)

[discordbotlist.com](https://discordbotlist.com)

[discord.boats](https://discord.boats)

[discordbots.co](https://discordbots.co)

[discord.bots.gg](https://discord.bots.gg)

[discordextremelist.xyz](https://discordextremelist.xyz)

[discordservices.net](https://discordservices.net)

[top.gg](https://top.gg)

## Troubleshooting

Please visit the [wiki](https://github.com/caneleex/BotListHandler/wiki/Troubleshooting) for troubleshooting steps. If the wiki doesn't contain a problem you're having, [open a new issue](https://github.com/caneleex/BotListHandler/issues/new).
