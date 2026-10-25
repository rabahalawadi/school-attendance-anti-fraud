# Anti-Fraud Verification Engine Specification & Threat Model

## 1. Threat Model & Adversary Assumptions

In educational environments, attendance fraud takes several distinct vectors:
1. **Remote Buddy Check-in via Shared Screenshots**: An absent student receives a photo/screenshot of the classroom QR code via WhatsApp/Telegram and submits check-in from home.
2. **Rotating Code Relay**: The absent student has an active messaging call with a present classmate who relays the 6-digit challenge code.
3. **GPS Coordinate Spoofing**: Android developer options or mock location apps simulating the school campus latitude/longitude.
4. **Credential Sharing**: Students sharing account credentials to allow one present student to log into multiple accounts from a single device.
5. **Replay & Timestamp Tampering**: Replaying captured HTTP check-in payloads or setting the device clock forward/backward.

---

## 2. Multi-Factor Countermeasures & Residual Risk

| Verification Factor | How It Mitigates Fraud | Limitations & Residual Risk | Countermeasure in System |
| :--- | :--- | :--- | :--- |
| **Method A: Registered Classroom Device** | Restricts check-in to school-owned classroom terminals/PCs. | Personal phones cannot satisfy this check if required. | Configurable as Optional (with risk escalation) or Required. Validates hardware kiosk credentials. |
| **Method B: Dynamic Rotating QR Code** | High-frequency rotation (default 15s) invalidates tokens before they can be forwarded and scanned remotely. | If video stream is open, a fast remote student might scan within 10 seconds. | Combined with 6-digit code or registered device; server time is strictly authoritative. |
| **Method C: Ephemeral 6-Digit Challenge** | Short-lived secondary challenge displayed on screen. | Can be typed and texted to a friend within 30s. | Strict rate-limiting (5 failed attempts locks IP/account). Expired challenges immediately rejected. |
| **Method D: GPS Geofencing** | Bounds check-in to school campus perimeter (e.g. 150m radius). | Desktop PCs lack GPS; mock location apps can spoof coordinates. | Marked as Optional/Advisory. Distance discrepancies elevate risk score to MEDIUM without blocking PCs. |
| **Initial 2-Week Manual Window** | Teachers review and visually confirm students during deployment. | Requires teacher attention during class. | Establishes baseline behavioral patterns and flags suspicious outliers. |

---

## 3. Evaluation Decision Table

| Method A (Device) | Method B (QR) | Method C (6-Digit) | Method D (GPS) | Initial Window | Overall Decision | Final Status | Verification Status |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| Pass | Pass | Pass | Pass | Inactive | **ACCEPTED** | `PRESENT` | `SERVER_VERIFIED` |
| Personal (Opt) | Pass | Pass | Pass | Inactive | **ACCEPTED** | `PRESENT` | `SERVER_VERIFIED` (Risk: Medium) |
| Personal (Opt) | Pass | Pass | Pass | Active | **ACCEPTED** | `PENDING_CONFIRMATION` | `PENDING_MANUAL` |
| Personal (Opt) | Expired (Req) | Any | Any | Any | **REJECTED** | `REJECTED` | `FLAGGED` |
| Failed (Req) | Any | Any | Any | Any | **REJECTED** | `REJECTED` | `FLAGGED` |
| Pass | Pass | Incorrect (Req) | Any | Any | **REJECTED** | `REJECTED` | `FLAGGED` |
| Disabled | Disabled | Pass | Disabled | Inactive | **ACCEPTED** | `PRESENT` | `SERVER_VERIFIED` |
