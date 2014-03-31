BLE Heart rate variability demo 
==============

Bluetooth low energy (Smart) Heart rate variability sensor demo app for Android

This app searches for nearby BLE devices and connects to heart rate sensor service. It shows hear rate (average pulse) and heart rate variability (beat-to-beat interval or RR data). The app also has a graphical demo of pulsing dot for real-time biofeedback monitoring of hear rate variability showing nicely how breathing rate affects heart rate variability.

Heart rate variability could be used to calculate different things like training effect on sports, sleep quality, stress and relaxation effects.

BLE is a nice since it has open protocol for transmitting data like heart rate here. It makes setting up a development environment relatively fast and you can concentrate on doing you actual business logic.

Bluetooh BLE is supported from Android 4.3 (API level 18) onwards. 
Read more about [Android BLE development][2].
There is also [Android sample code][2] for GATT services.

<ul>
   <li>
      <strong>Tested heart rate belts:</strong>
      <li>Polar H6</li>
   </li>
   <li>
      <strong>Android BLE devices:</strong>
      <li>Nexus 7 (Asus)</li>
   </li>
</ul>

## Use instructions
Connect heart rate BLE belt on your chest or wrist. Start the app and scan for BLE devices. Select heart rate sensor and wait for the GATT hear rate service to be found. Heart rate data values starts to update on the screen. Press demo button to show a graphical demo of heart rate variability. You will get nice graphics when you sit down relaxed. Try some slow deep breaths. The dot shows your heart rate variability cut from 700 milliseconds corresponding to pulse of 87. If your pulse is higher no dot is drawn. You can select GATT information services to read other data on the device.

## Troubleshooting
Android BLE seems to be slow to connect on first time. Please wait a minute or two to get the heart rate reading.
If device is disconnected some times switching Android bluetooth off and back on will help to reconnect.
Make sure your chest belt is well moisturized to get a good contact.

-------------------------------------------------------------------------------

Developed By
============

* Olli Erjanti - <olli@erjanti.fi>

Most of the bluetooth BLE sensor handling are from [TI Sensor Tag][1] project by:
* Steven Rudenko - <steven.rudenko@gmail.com>

Polygon drawing from Pro Android book by APress and [Sayed Hashimi and Satya Komatineni][4]. 

License
=======
```
The MIT License (MIT)

Copyright (c) 2013-2014 Steven Rudenko and Olli Erjanti

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```


[1]:https://github.com/StevenRudenko/BleSensorTag
[2]:http://developer.android.com/guide/topics/connectivity/bluetooth-le.html
[3]:http://developer.android.com/samples/BluetoothLeGatt/index.html
[4]:http://androidbook.com/akc/display?url=DisplayNoteIMPURL&reportId=3189&ownerUserId=android