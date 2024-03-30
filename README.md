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

Operators on the server can take pictures with the command `/takepicture`
## Pictures

<img src="https://i.imgur.com/Bzi99fL.png" width="128">
<img src="https://i.imgur.com/YRiBxGn.png" width="128">
<img src="https://i.imgur.com/pstXzfc.png" width="128">

## Limitations
* Each surface displays just one color. If you see any colors that feels off, let me know.
* Players and mobs won't show up on pictures - *X-Files theme plays*
* Image are distorted when looking up/down.

## Todo
* [Bug] `renderAsync: true` seems broken and might crash the server
* [QA] Test with Geyser
* [Feature] `/takepicture` seems to ignore the requirement of paper
  * Make paper requirement optional in the config
* [Optimize] See if loading maps at boot can be improved (async? sqlite?)
* [Optimize] Prefix all console logging with `[Camera]`
* [Optimize] Don't take picture, when cancelling placement of camera.
* [Optimize] Check if downloading the texture pack zip is needed (389 KB). If texture pack is needed:
  * Move texture pack zip to this repository 
  * Upgrade texture pack to 1.20.4
  * Make the url to texture pack configurable
* [Optimize] See if it's possible to remove despawned images from the `maps` folder
* [Feature] Add option to place camera on the ground (might conflict with claims plugin).

## Issues

Any features requests or issues should be made through here:

https://github.com/SnorklingTurtle/Minecraft-Camera/issues
