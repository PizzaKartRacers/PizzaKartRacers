# PizzaKartRacers Minecraft Server

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.4-brightgreen)
![License](https://img.shields.io/badge/License-CC0-blue.svg)
![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen)

PizzaKartRacers is a opensource copy of the famous hypixel gamemode TurboKartRacers, this is a from scratch project (not using any actual source code from TKR). This project was created due to the fact that Hypixel doesn't update their gamemode so making a rewrite is the only way to go.

## Help By Contributing!

Any help would be amazing, If you want to know what needs to be added or fixed, here is the [Trello board](https://trello.com/b/ntrrAWuQ/pizzakartracers)!
## License

This project is licensed under the CC0 License - see the [LICENSE](LICENSE) file for details.

## Please Keep In Mind

I plan on making a server with this with a better server system (dynamically start/stop servers by how many players) so I do not plan to have easy support if it's a singular server. If you want to create a fork that does it then be my guest but it will probably not be added to the main branch (Will link to it though so others can use it).

# How To Use

## Download the jar (or build it yourself)
You can download the jar in the [releases section](https://github.com/PizzaKartRacers/PizzaKartRacers/releases)

## Create Extra Files
You will need to create a maps.yml file and input the data (can be found in the source code, will make it auto create later).

You will also need a bungeeGuardToken.yml file where it stores the secret code for your proxy server (will make this optional at some point).

You will also need to provide the worlds in a maps folder on the machine, this is so the program can grab them and use them when needed.

## Running The Program.
To run the program, you just need to do `java -jar PizzaKart-VERSION.jar` Currently the jar requires Java 22 to run.
