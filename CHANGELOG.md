# Changelog

## v1.0.0 - 2026-09-21

Initial release.

- Forward incoming SMS messages that match sender/text filters to phone numbers and/or a webhook URL.
- Fully offline except for the single, explicit webhook POST configured by the user.
- Persian-only UI with the Vazirmatn font bundled for offline use.
- Permissions screen to help grant SMS/notification permissions, disable battery optimization,
  and jump to app settings for OEM autostart configuration.
- Foreground service + boot receiver to keep forwarding alive across reboots.
