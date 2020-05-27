# [Hologram-CMA](https://hologram-iot-service.github.io/Hologram-CMA/)
🌌 Hologram Elas central management application for controling the IoT core systems.

### Build Status
![Android CI](https://github.com/Hologram-IoT-Service/Hologram-CMA/workflows/Android%20CI/badge.svg)

### Download
[![jitpack](https://jitpack.io/v/Hologram-IoT-Service/Hologram-CMA.svg)](https://jitpack.io/#Hologram-IoT-Service/Hologram-CMA) <br>
Here is the `jarvis-data` model library that used as common thrid-party library. <br>
This library is used in `Holohram-CMA` and `Holohram-Hardware-core` project. <br>

#### Step1
Add the JitPack repository to your build file. <br>
Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
#### Step2
Add the dependency.
```gradle
dependencies {
   implementation 'com.github.Hologram-IoT-Service:Hologram-CMA:1.0.0'
}
```

### How to use?
This CMA library provides below `DatabaseReference` extensions.
```kotlin
// send `turn on` command to the Elsa-Heart.
ledTurnOn()

// send `turn off` command to the Elsa-Heart.
ledTurnOff()

// send a customized message to the Elsa-Heart.
// the message format is composed with name (tag) and the message.
// Elsa-heart will be get this message and process based on the message information.
sendMessage(name: String, message: String)
```

#### getFireBaseMessage
We can get pre-existed message string type from a sentence using the `getFireBaseMessage` method.<br>
If a sentence includes any pre-existed word string, we can get processed command message as a String type.
```kotin
if (getFireBaseMessage(command) == command.trim()) {
     toast("query : $result")
     queryServer(command)
}
```

#### sendCommandMessage
We can send command messages to the Elsa-Heart directly using this functionality.<br>
`sendCommandMessage` requires an instance of the `DatabaseReference` and a `message`as arguments. <br>
Here is the basic example of the usage.<br>
```kotlin
private val databaseReference = fireBaseDatabase.reference

... // skip body // ...

val command = result.replace(commanderName, "").trim()
sendCommandMessage(databaseReference, command)
```

### Used Tech Stacks
- Kotlin based with Java.
- Android AppCompat
- Bluetooth BLE - Communicate using bluetooth ble 4.0 service for connecting bluetooth devices.
- Coroutines - Implement asynchronous works over the language level (Kotlin).  
- Firebase - Remote database for persisting control data from the core and hardware decives.
- DialogFlow - Query sentences to the remote server and takes answers.

### Architecture
![screenshot1066765837](https://user-images.githubusercontent.com/27774870/80310532-2c2c9a00-8816-11ea-9f0e-0e13de4dcec5.png)

### Reference
https://github.com/skydoves/MagicLight-Controller

### Overview
Asynchronous or non-blocking programming is the new reality. Whether we're creating server-side, desktop or mobile applications, it's important that we provide an experience that is not only fluid from the user's perspective, but scalable when needed.

![coroutines-xenonstack](https://user-images.githubusercontent.com/27774870/80310260-b7a52b80-8814-11ea-8bbe-f1740a4b6301.png)


# License
```xml
Copyright 2020 rurimo

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
