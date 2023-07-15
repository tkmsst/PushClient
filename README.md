# Push Client

The Android app to launch any app automatically when a notification is received.

## Prerequisites

Install an app to be launched upon a push notification.

Set up a server to store Registration tokens and send push notifications.

## Sample files

A package name of an app to be launched should be specified in a script file.

Refer to the [samples](samples) directory for sample scripts and configuration files for [Asterisk](https://www.asterisk.org/).

### Scripts

| File     | Description                       |
| -------- | --------------------------------- |
| push.bat | Push batch file for Windows       |
| push.sh  | Push script for Linux             |
| push.dat | Registration token list           |
| reg.php  | PHP script for token registration |

### Asterisk

| File            | Description |
| --------------- | ----------- |
| extensions.conf | Dialplan    |
| queues.conf     | Call queues |

### Note

For full functionality, the followings settings need to be allowed on your device.

- Notification
- Modify system settings
- Display over other apps
- Ignore battery optimisation

---
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, and/or distribute. However, the sale of the Software is strictly prohibited.
