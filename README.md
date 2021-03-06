# Floc Crisis Center Platform

Build and deploy infobots using [Rasa](https://github.com/RasaHQ/rasa)

## Introduction
Floc Crisis Center (FCC) is an open source tool that helps you quickly build infobots. These domain specific infobots can hold simple conversations with the user to provide topical information in the form of menus and submenus.

## Repos
FCC is divided into 4 repositories:
* [Platform](https://github.com/floc-crisis-center/platform) (this project)
* [Bot Builder](https://github.com/floc-crisis-center/bot-builder)
* [Web Console](https://github.com/floc-crisis-center/web-console)
* [Infobot Template](https://github.com/floc-crisis-center/infobot-template)

## Development
FCC platform is developed on top of [vert.x](http://vertx.io/) using JDK 11.

To start contributing, do the following:
* Install the latest version of JDK 11.
* Use git to clone the source code from https://github.com/floc-crisis-center/platform
* FCC uses maven. Build the code by running the maven wrapper "mvnw clean install"
* To start the server, run "java -jar target/crisis-center-1.0.0-SNAPSHOT-fat.jar"
