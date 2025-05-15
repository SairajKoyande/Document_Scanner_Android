📄 Smart Document Scanner App (Android)
This Android application is a powerful, fast, and intelligent document scanner built using Google ML Kit and Java. The app automatically detects and captures documents, applies advanced filters like grayscale and black & white, removes shadows, auto-crops documents, and stores them in a local database for easy access and management.

✨ Features
📸 Automatic Document Detection & Capture
Uses Google ML Kit to detect edges and auto-capture documents with stability tracking.

🖐️ Manual Capture Support
Allows manual photo capture with edge detection.

✂️ Auto Crop
Automatically detects document corners and crops the scanned image.

🎨 Smart Filters
Grayscale
Black & White
Color enhancement
Shadow removal

💾 Document Storage
Saves scanned documents in local SQLite/Room database with thumbnail previews.

🗂️ Gallery View
Lists all scanned documents in a scrollable, user-friendly interface.

🔍 Zoom & View
Tap on a document to zoom and view in full resolution.

🧾 PDF Export (Optional if implemented)
Converts multiple scanned pages into a single PDF (if supported in your build).

🧰 Tech Stack
Technology	Purpose
Java	Core app logic
XML	UI Layouts
Google ML Kit	Document detection & image processing
CameraX / Camera2	Camera preview and image capture
SQLite / Room	Local database storage for scanned docs
Glide	Image loading and thumbnail rendering
AndroidX Libraries	UI and lifecycle support

https://github.com/user-attachments/assets/9ea639d7-1f25-46dd-b2b5-6222557cbb9b
