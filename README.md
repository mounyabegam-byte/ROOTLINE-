# EduPulse AI

EduPulse AI is an Android app built for modern academic monitoring, early-risk detection, and data-driven student support. It brings together student performance, attendance, intervention tracking, and AI-guided recommendations in a single mobile experience for students, faculty, HODs, principals, and administrators.

This project is the Android client for the ROOTLINE- workspace and uses Jetpack Compose, Firebase, Room, and the Gemini AI SDK to deliver a smart academic management dashboard.

## Overview

The app helps educational institutions move from reactive support to proactive intervention by surfacing:

- academic risk trends for individual students and cohorts
- subject-wise performance insights and topic gaps
- attendance monitoring and recovery plans
- faculty/admin workflows for intervention and reporting
- AI-generated study advice and academic guidance
- role-based dashboards and authentication

## Core Features

### Student Experience
- Personalized dashboard with student metrics and summaries
- Subject grade tracking and performance trends
- Attendance overview and class participation insights
- AI advisor for academic guidance and study planning
- Study plan generation and progress tracking
- Notifications and intervention alerts
- Student profile and performance drill-down screens

### Faculty and Staff Experience
- Faculty dashboard for student progression and intervention planning
- Update internal/assignment/lab marks
- Record attendance and monitor attendance trends
- Student selection and profile review
- Risk-based prioritization for support actions

### Leadership and Admin Experience
- Department-level and institutional dashboards
- High-risk student visibility and academic health summaries
- Data import and management workflows
- Role switching between student, faculty, HOD, principal, and admin views
- Institutional analytics and intervention oversight

### AI Capabilities
- Gemini-powered academic guidance assistant
- Risk explanation and insight generation
- Personalized recommendations based on subject performance
- Quick actions for study planning, attendance recovery, and improvement strategies

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Android Architecture Components
- ViewModel + StateFlow
- Room Database
- Firebase Authentication
- Firebase Firestore / realtime data features
- Firebase AI / Gemini
- Retrofit + Moshi + OkHttp
- Kotlin Coroutines
- Gradle Kotlin DSL
- Secrets Gradle Plugin

## Project Structure

```text
ROOTLINE-/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/
│   │   │   │   ├── model/
│   │   │   │   ├── ui/
│   │   │   │   └── MainActivity.kt
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradle/libs.versions.toml
├── README.md
├── metadata.json
├── local.properties
└── .env / .env.example
```

## App Roles

The app supports several user roles:

- Student
- Mentor
- Faculty
- Staff
- HOD / Dept Head
- Principal
- Super Admin

The UI dynamically adapts based on the selected role and current authenticated user.

## Prerequisites

Before running the app, make sure you have:

- Android Studio Hedgehog or newer
- JDK 11+
- Android SDK 36 configured in Android Studio
- An Android emulator or physical device
- Firebase project with Android app registration
- Google Services config file for Firebase

## Setup Instructions

### 1. Clone the project

```bash
git clone <repository-url>
cd ROOTLINE-
```

### 2. Open in Android Studio

Open the project folder in Android Studio and let Gradle sync complete.

### 3. Configure Firebase

This project is set up for Firebase services and includes Google Services configuration support.

- Create a Firebase project in the Firebase Console
- Add an Android app with the package ID used by the project
- Download `google-services.json` and place it in the `app/` directory
- Ensure Firebase Auth and Firebase AI services are enabled for your project

### 4. Configure secrets and environment values

The project uses the Secrets Gradle Plugin and expects environment/development config files such as:

- `.env`
- `.env.example`

You may need to add any project-specific keys or values required by your Firebase setup before running the app.

### 5. Release signing setup

The app declares a release signing configuration with environment variables:

- `KEYSTORE_PATH`
- `STORE_PASSWORD`
- `KEY_PASSWORD`

For local debug builds, the project uses a debug keystore when available.

## Build and Run

### Debug build

```bash
./gradlew assembleDebug
```

### Install on connected device or emulator

```bash
./gradlew installDebug
```

### Run tests

```bash
./gradlew test
```

## Important Notes

- The app is configured with `namespace = "com.example"` in the module build file, while the application ID in `defaultConfig` is `com.aistudio.edupulse.qzvkwm`.
- Some Firebase dependencies are included and the project is designed to work with Firebase-driven auth/data flows.
- If `google-services.json` is missing, Gradle may warn during sync, and Firebase features may not work until it is added.
- The app includes mock/sample academic data to demonstrate dashboard and analytics flows.

## Architecture Summary

The app follows a role-based dashboard pattern with Compose UI and MVVM-style state management:

- `MainViewModel` manages app-wide state, selected student, AI chat, and role-driven app navigation
- `EduPulseApp` controls the main navigation and screen switching
- Screen modules handle domain-specific dashboards and academic workflows
- `Room` is used for local persistence of student records, grades, tasks, badges, and interventions
- Firebase services support authentication and cloud-backed behavior
- Gemini AI powers chat-based academic recommendations

## Example User Flows

- A student logs in and sees their risk profile, course performance, and study plan
- A faculty member updates marks and attendance for a class
- An admin reviews high-risk students across departments and triggers interventions
- A user asks the AI assistant for a recovery plan or strategy to improve performance in a subject

## Future Improvements

This project is already feature-rich for a demo/prototype, but common next steps include:

- production-grade Firebase security rules
- real backend sync and data validation
- analytics export/report generation
- onboarding and profile customization
- dark/light theme refinement and accessibility improvements
- deployment pipelines for QA and production builds

## License

No explicit license file is present in this repository yet. If this project is meant for public or commercial distribution, add an appropriate license file before release.

## Contact / Maintainer

This project is currently a local workspace repository without a formal maintainer record in the repo. Use the repository owner or team contact for production support and distribution questions.

---

This README was prepared to reflect the current Android app structure and behavior visible in the codebase. For exact production onboarding details, always confirm any Firebase, signing, or environment configuration with the repository owner or project team.

