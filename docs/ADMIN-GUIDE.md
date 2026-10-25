# Administrator User Guide

## 1. Academic Hierarchy Management
1. Navigate to **Academic Structure** to create academic years, grade levels, sections, and subjects.
2. Capacity limits can be assigned per section.
3. Historical records are preserved when archiving previous years.

## 2. Excel Import & Promotion
1. Download the sample `.xlsx` template from the **Excel Import Wizard**.
2. Upload the spreadsheet to receive a pre-import report detailing valid rows, conflicts, and duplicate IDs.
3. Confirm to commit valid records into the database within an isolated transaction.
4. Use **Manual Promotion** to select eligible students, choose target grade/section, preview, and commit without altering historical records.

## 3. Anti-Fraud Policy Configuration
1. Open the **Anti-Fraud Engine** configuration tab.
2. Toggle individual factors:
   - **Method A (Registered Classroom Device)**: Require school-owned kiosk PC or keep optional.
   - **Method B (Dynamic Rotating QR)**: Set rotation interval (default 15s).
   - **Method C (6-Digit Challenge Code)**: Set expiration interval (default 30s).
   - **Method D (GPS Geofencing)**: Set campus center coordinates and permitted bounding radius.
   - **Initial Manual Window**: Set grace period duration (default 14 days) during which teacher visual confirmation is mandatory.
3. Click **Save & Deploy Policy** to update the active policy version.
