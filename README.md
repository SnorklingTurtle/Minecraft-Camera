Minecraft Camera
================

Minecraft Camera plugin for Spigot. Adds craft-able cameras to your Minecraft server and the ability to take pictures. 

This is an updated version of the [Cameras plugin by Cup0fCode](https://github.com/Cup0fCode/Cameras/). Tested on Minecraft 1.20.4.

## Installation

1. [Download the plugin](https://github.com/SnorklingTurtle/Minecraft-Camera/releases/)
2. Move the jar-file into the server plugin-folder
3. Start the server

If you don't already have a config file, one will be created on first boot of the server. The config is located at `plugins/Minecraft-Camera/config.yml`. Restart the server after editing the config. 

Images are saved as txt-files in `plugins/Minecraft-Camera/maps`. Each file is usually at a size of 3-12 KB.

## Usage
Players can craft cameras using the following recipe:

![crafting](https://i.imgur.com/GsrxLPY.png)

Operators on the server (`/op <username>`) can take pictures with the command `/takepicture`. When using this command a camera and paper is 
not required, however you still need space in your inventory.

### Permissions

If `permissions` is se to `true` in config.yml, you can use the following in permissions.yml:

```
# permissions.yml
cameras.craft:
  description: Allow crafting camera
  default: true
cameras.useitem:
  description: Allow using the camera
  default: false
cameras.command:
  description: Allow the command /takepicture
  default: false
```


## Pictures

<img src="https://i.imgur.com/Bzi99fL.png" width="128">
<img src="https://i.imgur.com/YRiBxGn.png" width="128">
<img src="https://i.imgur.com/pstXzfc.png" width="128">

## Limitations
* Each surface displays just one color. If you see any colors that feels off, let me know.
* Players and mobs won't show up on pictures - *X-Files theme plays*
* Image are distorted when looking up/down.

## Todo
* [QA] Test with Geyser
* [QA] See if permissions works as intended
* [Optimize] Can converting colors from string be improved
* [Optimize] See if loading maps at boot can be improved (async? sqlite?)
* [Optimize] See if it's possible to remove despawned images from the `maps` folder
* [Feature] Add option to place camera on the ground (might conflict with claims plugin).

## Done

* [Bug] When shooting multiple times, subsequent pictures will stop rendering when the first one finishes
* [Feature] Let sky color depend on time of day
* [Optimize] Set default render distance to 128 (instead of 256)
* [Feature] Allow changing render distance from config
* [Optimize] Don't take picture, when cancelling placement of camera.
* [Optimize] No need to read all every single image at server boot. Read colors from a color-mapping.config instead
* [Optimize] Prefix all console logging with `[Camera]`
* [Optimize] Render X amount of pixels per tick, for better performance
* [Bug] It's possible to take pictures with a full inventory using `/takepicture`
* [QA] Make paper requirement optional in the config

## Issues

Any features requests or issues should be made through here:

https://github.com/SnorklingTurtle/Minecraft-Camera/issues
