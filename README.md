📄 README.md
# Swasth Punjab 🤖🇮🇳

Swasth Punjab is an AI-powered health assistant Android app designed to help users in Punjab access basic medical triage, symptom tracking, and doctor consultations in their preferred language—Hindi, English, or Punjabi.

## 🧠 Features

- 🔍 **Symptom Checker**: Users can select symptoms and severity to receive basic health advice.
- 🗣️ **Voice Input**: Speak symptoms using built-in speech recognition.
- 🧑‍⚕️ **Doctor Consultation**: One-click access to a virtual consultation room via Jitsi Meet.
- 🌐 **Multilingual Support**: Full UI translation using ML Kit (Hindi, Punjabi, English).
- 🔊 **Text-to-Speech**: Health advice is spoken aloud in the selected language.
- 🤖 **Chatbot Interface**: Interactive form-based chatbot for health triage.

## 📱 Screens

- Language Selection Screen
- Patient/Doctor Role Selection
- Chatbot Symptom Form
- Virtual Doctor Room (Jitsi Meet)

## 🚀 Getting Started

### Prerequisites

- Android Studio (Arctic Fox or newer)
- Internet connection (for ML Kit translation model download)

🌍 Language Support
Users choose their preferred language on first launch. The app remembers this choice and translates all UI elements using ML Kit:
- hi – Hindi
- pa – Punjabi
- en – English (default)

🧪 How It Works
- ML Kit downloads the translation model on first use.
- All visible text (TextViews, Buttons, CheckBoxes, EditText hints) is dynamically translated.
- Text-to-Speech uses the selected locale for spoken output.
📸 Screenshots
Add screenshots of your app here (optional)

🛠️ Future Improvements
- Add Firebase for user authentication
- Store symptom history
- Integrate real-time chat with doctors
- Offline translation support

🙌 Credits
This project was proudly developed by the Swasth Punjab Team:
- Tripti Sharma
- Ritesh Pathak
- Rupesh Sharma
- Shivam Yadav
- Priyanshu Rai
- Rajeev Ranjan

Together, we built an AI-powered health assistant to support multilingual access to medical guidance across Punjab and beyond.

📄 License
This project is open-source under the MIT License.

