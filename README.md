🧠 MindMate: An AI-Powered Mental Health Support App

“Your mind deserves care too and MindMate is here to listen.”

🌍 Overview

MindMate is a modern Android application designed to support mental well-being through AI-driven mood detection, personalized journaling, and emotional assistance.
Built with empathy and powered by technology, MindMate enables users to track their emotions, talk with an intelligent chatbot, and receive curated wellness tips all in one app.

💡 Features

✨ Mood Detection via Camera (AI-powered)
Detects facial emotions such as happiness, sadness, anger, fear, and surprise using Google Cloud Vision API.

💬 AI Chatbot for Emotional Support
Chat naturally with an AI trained to offer comfort, motivation, and mindfulness guidance.

📖 Personal Journal
Log your daily thoughts, track emotional patterns, and view insights over time.

🧘 Mindfulness & Meditation Tools
Access curated breathing exercises and mental relaxation routines.

🌈 Dashboard & Analytics
See your emotional trends visualized with interactive graphs and statistics.

🔐 Firebase Authentication
Sign in securely via Email, Google, or other providers.

☁️ Real-Time Database
Stores and syncs mood data and user sessions instantly across devices.

🛠️ Tech Stack
Category	Tools / Libraries
Language	Java
Framework	Android SDK
Backend / Database	Firebase (Auth, Realtime Database, Storage)
AI & Vision	Google Cloud Vision API
UI/UX	Material Design 3, RecyclerView, CardView
Networking	OkHttp, Gson
Image Handling	Glide
Build System	Gradle
⚙️ Setup Instructions
1️⃣ Clone the repository
git clone https://github.com/michealamanya/MindMate.git
cd MindMate

2️⃣ Open in Android Studio

Go to File > Open, and select the project folder.

Let Gradle sync and install dependencies.

3️⃣ Set up Firebase

Go to Firebase Console
.

Create a project and add your Android app.

Download the google-services.json file and place it inside app/.

4️⃣ Set up Google Cloud Vision

Visit Google Cloud Console
.

Enable the Vision API.

Generate an API key and restrict it to Android or specific services.

Add the key to your app (e.g., in strings.xml or a secure config file).

5️⃣ Run the app

Plug in your device or use an emulator, then hit:

Run ▶️

🧠 How AI Mood Detection Works

The app activates the camera and captures a facial image.

The image is encoded in Base64 and sent securely to the Google Cloud Vision API.

The API returns emotion likelihoods (joy, sorrow, anger, etc.).

MindMate interprets this data and displays the user’s mood dynamically on the dashboard.

🧩 Folder Structure
MindMate/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/micheal/must/signuplogin/
│   │   │   │   ├── DashboardActivity.java
│   │   │   │   ├── ChatActivity.java
│   │   │   │   ├── JournalActivity.java
│   │   │   │   ├── CommunityActivity.java
│   │   │   │   └── MoreOptionsActivity.java
│   │   │   └── res/
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│
└── README.md

🧩 Future Enhancements

🔊 Voice-based emotion analysis

🧍‍♀️ Personalized AI therapy suggestions

📊 Advanced mood analytics dashboard

🌐 Offline AI inference with TensorFlow Lite

💬multilingual support for global accessibility

🧑‍💻 Author

Amanya Micheal
🎓 Computer Science Student @ Mbarara University of Science and Technology
💼 Passionate about AI, mental health tech, and mobile development
📫 Reach me via: LinkedIn
 | Email: amanyamicheal770@gmail.com
 | GitHub: github.com/michealamanya

⚖️ License

This project is licensed under the MIT License free to use, modify, and distribute with attribution.

💬 Quote

“Technology should heal hearts as much as it connects minds.”
— Amanya Micheal
