<!-- d6ea6d22-9fa3-49ac-98c7-2a0ea66c38ff bd801486-681f-4b35-b641-bef92b89de44 -->
# Speed Reader Trainer - Android App Plan

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (modern, declarative, recommended for new Android projects)
- **Database/Auth:** Firebase (Authentication + Firestore)
- **AI:** OpenAI API (GPT-3.5/4 for comprehension questions)
- **File Parsing:** Apache PDFBox (PDF), standard Kotlin (TXT/MD)

## Architecture Overview

```
app/
├── ui/           # Compose screens & components
├── data/         # Repository pattern, Firebase, OpenAI service
├── domain/       # Use cases, models
└── di/           # Dependency injection (Hilt)
```

## Core Features & Screens

### 1. Authentication Flow

- Sign up / Login with Firebase Auth (email/password or Google Sign-In)
- First-time user detection to force baseline test

### 2. Baseline Test Screen

- Pre-loaded test passage (~500 words) with known difficulty
- Timer tracks reading duration automatically
- Followed by 5-10 comprehension questions (pre-written for baseline)
- Calculates and stores: **baseline WPM** and **comprehension score**

### 3. Home/Dashboard Screen

- Display current stats (WPM, comprehension %, sessions completed)
- "Upload Document" button
- "Practice" button (use previously uploaded docs)
- Progress chart showing improvement over time

### 4. Document Upload & Processing

- File picker for PDF, TXT, MD files
- **PDF parsing:** Use PdfBox-Android or iText
- **Text extraction:** Clean and segment into chunks/passages
- Store document metadata in Firestore, text content locally (Room cache)

### 5. Speed Reading Practice Screen (RSVP Mode)

- Words displayed one at a time at center of screen
- Adjustable WPM slider (start at user's baseline, allow +/- adjustment)
- Play/Pause controls
- Progress indicator (% through passage)
- Optional: chunk mode (2-3 words at a time)

### 6. Comprehension Quiz Screen

- After each practice session, call OpenAI API to generate 3-5 questions
- Multiple choice format
- Score displayed with feedback
- Results saved to Firestore

### 7. Progress & Stats Screen

- Historical WPM progression chart
- Comprehension score trends
- Total reading time, documents completed
- Streak tracking (daily practice)

## Data Models (Firestore)

```kotlin
// Users collection
data class UserProfile(
    val uid: String,
    val email: String,
    val baselineWpm: Int,
    val baselineComprehension: Float,
    val createdAt: Timestamp
)

// Reading sessions sub-collection
data class ReadingSession(
    val id: String,
    val documentId: String,
    val wpmUsed: Int,
    val comprehensionScore: Float,
    val durationSeconds: Int,
    val completedAt: Timestamp
)

// Documents collection
data class UserDocument(
    val id: String,
    val userId: String,
    val title: String,
    val wordCount: Int,
    val uploadedAt: Timestamp
)
```

## What You Need to Provide

1. **OpenAI API Key** - Get from [platform.openai.com](https://platform.openai.com)
2. **Firebase Project** - Create at [console.firebase.google.com](https://console.firebase.google.com):

   - Enable Authentication (Email/Password and/or Google)
   - Create Firestore database
   - Download `google-services.json` and place in `app/` folder

3. **Baseline Test Content** - A ~500 word passage + 5-10 hand-written comprehension questions for the initial test

## Implementation Order

Phase 1: Project setup, Firebase integration, auth flow

Phase 2: Baseline test screen with timer and scoring

Phase 3: Document upload and text extraction (PDF/TXT/MD)

Phase 4: RSVP speed reading engine and practice screen

Phase 5: OpenAI integration for comprehension quiz generation

Phase 6: Progress tracking, stats dashboard, charts

Phase 7: Polish - settings, themes, error handling

### To-dos

- [ ] Create Android project with Kotlin, Compose, Hilt, and Firebase dependencies
- [ ] Implement Firebase Auth (sign up, login, Google sign-in)
- [ ] Build baseline test screen with timer, passage display, and comprehension quiz
- [ ] Implement file picker and PDF/TXT/MD parsing
- [ ] Build RSVP speed reading screen with WPM control
- [ ] Integrate OpenAI API for generating comprehension questions
- [ ] Create dashboard with stats and progress charts
- [ ] Add settings, error handling, and UI polish