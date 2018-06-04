# adobe-aem-plugin
Carry on from IntelliJ Plugin

## Installation

* Download the adobe-aem-plugin.jar from this repo
* Go to IntelliJ -> Preferences -> Plugins -> Install plugin from disk -> adobe-aem-plugin.jar

## Usage

* Right click on folder you want to create a component -> AEM -> Create Component.
* Use the dialog to input details.
* Sometimes it may take 5-10 seconds for IntelliJ to refresh the file structure view.

## Development

Continued from my repo: https://github.com/jackkenlay/intellij-adobe-aem


To run,
```gradle clean build```
click on the gradle window - Tasks -> IntelliJ -> runIde


## TODO
 * Set focus of input text field
 * right click - create client libs
 * right click - create client libs -> all
 * right click - create client libs -> js
 * right click - create client libs -> css
 * Configurable settings
 *  - default component group
 *  - if null, then can save new entry
 *  - default html/less/js templates (hard)
 * Ensure dialog is smooth experience with keyboard
 * Test export
 * Round off ReadME
 * have checker for less or SASS
 * auto find components folder for keyboard shortcut