# Screenshot

Windows/Linux Utility to select screenshots

### Download

---

Download the latest binary [here](bin/Screenshot.jar)

[Java](https://java.com/download) is required.

### Usage

---

Running the Jar file will start the program, but there are other ways to use the program:

Open a terminal/command line in the jar's folder, and try some of these comamnds

`java -jar Screenshot.jar --startup` Copy the program to startup
`java -jar Screenshot.jar --auto-capture` Start capturing without the need to press PrintScreen
`java -jar Screenshot.jar --help` See other flags

### Configuration

---

Configuration for Screenshot is stored in seperate files in the config folder

**Windows:** `C:\Users\YourName\AppData\Roaming\Screenshot`

**Linux:** `/home/YourName/.config/Screenshot`

### Settings

---

* **captureDelay** Delay, in milliseconds, until the Screenshot is captured after selection
* **deleteTime** Time, in seconds until uploaded images are deleted
* **openDelay** Delay, in milliseconds, before the program opens. Useful at startup, to prevent issues
* **hashes** Storage file for image uploads, to delete. Recommended to not edit

* **theme** Configuration for application theme, such as colors and sizes.
* **modifiers** Key shortcuts for certain actions, like CTRL+SHIFT+PSC. Use integer values

### Donate

---

Donating will never be required for access to my projects, but I would appreciate it a lot!

[![paypal](https://img.shields.io/badge/donate-paypal-brightgreen.svg)](https://paypal.me/supportnathan)
