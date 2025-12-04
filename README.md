# Speed Reader Trainer

An Android app to help users improve their reading speed while maintaining comprehension using the RSVP (Rapid Serial Visual Presentation) technique.

## Features

- **Baseline Test**: Measure your starting reading speed and comprehension
- **Document Upload**: Support for PDF, TXT, and Markdown files
- **RSVP Speed Reading**: Words displayed one at a time with adjustable WPM
- **AI Comprehension Quizzes**: OpenAI-generated questions after each session
- **Progress Tracking**: Track your improvement over time
- **User Accounts**: Firebase authentication with cloud sync

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **DI**: Hilt
- **Backend**: Firebase (Auth + Firestore)
- **AI**: OpenAI API (GPT-3.5/4)
- **PDF Parsing**: PDFBox Android

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/speed-reader-trainer.git
cd speed-reader-trainer
```

### 2. Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project
3. Add an Android app with package name `com.speedreader.trainer`
4. Download `google-services.json` and place it in the `app/` folder
5. Enable **Authentication** (Email/Password and Google Sign-In)
6. Create a **Firestore Database** in production mode

### 3. OpenAI API Key

1. Get an API key from [OpenAI Platform](https://platform.openai.com)
2. Add to `local.properties`:

```properties
OPENAI_API_KEY=your_api_key_here
```

### 4. Google Sign-In (Optional)

1. In Firebase Console, go to Authentication > Sign-in method > Google
2. Enable and configure
3. Copy the Web Client ID
4. Replace `YOUR_WEB_CLIENT_ID` in `app/src/main/res/values/strings.xml`

### 5. Build and Run

```bash
./gradlew assembleDebug
```

Or open in Android Studio and run.

## Project Structure

```
app/src/main/java/com/speedreader/trainer/
├── data/
│   ├── remote/          # OpenAI API service
│   └── repository/      # Data repositories
├── di/                  # Hilt dependency injection
├── domain/
│   └── model/           # Data classes
└── ui/
    ├── navigation/      # Navigation setup
    ├── screens/         # All app screens
    │   ├── auth/        # Login & Register
    │   ├── baseline/    # Baseline test flow
    │   ├── dashboard/   # Main dashboard
    │   ├── document/    # Upload & list documents
    │   ├── progress/    # Progress tracking
    │   ├── quiz/        # Comprehension quiz
    │   ├── reading/     # RSVP speed reading
    │   ├── settings/    # App settings
    │   └── splash/      # Splash screen
    └── theme/           # Colors, typography, theme
```

## How It Works

1. **Sign Up/Login**: Create an account or sign in
2. **Baseline Test**: Read a passage and answer questions to establish your baseline WPM
3. **Upload Documents**: Add your own PDF, TXT, or MD files
4. **Practice**: Use RSVP mode to read at controlled speeds
5. **Quiz**: Answer AI-generated comprehension questions
6. **Track Progress**: View your improvement over time

## License

MIT License

