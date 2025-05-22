# CineChord
> CineChord: An Android application for watching videos synchronously and chatting with friends, built with Firebase and Jetpack Compose.

# CineChord - Co-Watching Application

CineChord is an Android application that allows you to watch videos synchronously with your friends and chat at the same time. This application is developed using Firebase and Jetpack Compose.

## Features

*   User registration and email/password authentication with Firebase.
*   Creation of new watch rooms (name, description, video URL).
*   Joining existing rooms.
*   Synchronized video playback within rooms (supports YouTube videos).
*   Real-time chat within rooms.
*   Listing rooms the user has joined and created.
*   Modern and user-friendly interface (built with Jetpack Compose).

## Technology Stack

*   **Programming Language:** Kotlin
*   **UI:** Jetpack Compose
*   **Architecture:** Clean Architecture-like structure (MVVM, Use Cases, Repository Pattern)
*   **Asynchronous Operations:** Kotlin Coroutines & Flow
*   **Database & Backend:** Firebase
    *   Authentication
    *   Realtime Database (For rooms, messages, playback state)
    *   App Check (Application Security)
*   **Dependency Injection:** Hilt
*   **Video Player:** Android YouTube Player (WebView-based) / Potentially Media3 ExoPlayer
*   **Image Loading:** Coil (For profile pictures, etc.)
*   **Navigation:** Jetpack Navigation Compose

## Setup and Running

1.  **Clone the Project:**
    ```bash
    git clone https://github.com/YOUR_USERNAME/YOUR_REPOSITORY_NAME.git
    cd YOUR_REPOSITORY_NAME
    ```
2.  **Firebase Setup:**
    *   Create a new project in the Firebase console ([https://console.firebase.google.com/](https://console.firebase.google.com/)).
    *   Add your Android application to your Firebase project (package name: `com.example.cinechord`).
    *   Download the `google-services.json` file from your Firebase project.
    *   Copy the downloaded `google-services.json` file into the `app` folder of your project (`CineChord/app/google-services.json`). **This file is ignored by `.gitignore` and should not be uploaded to GitHub.**
    *   In the Firebase console, go to the **Authentication** section, and under the "Sign-in method" tab, enable the **Email/Password** provider.
    *   In the Firebase console, go to the **Realtime Database** section:
        *   Create a database (you can start in test mode or locked mode).
        *   Update the **Rules** tab as follows:
            ```json
            {
              "rules": {
                ".read": "auth != null",
                ".write": "auth != null"
              }
            }
            ```
    *   In the Firebase console, go to the **App Check** section:
        *   Register your app.
        *   Enable the **Play Integrity** and **Debug** providers.
        *   Add the SHA fingerprints (SHA-1 and SHA-256) from Android Studio to your Firebase project's Android app settings (you can get them with the `./gradlew signingReport` command).
        *   If you encounter App Check issues while testing on an emulator or physical device, get the debug token from Logcat and add it to the "Debug tokens" section in the Firebase App Check console.
3.  **Open the Project in Android Studio:**
    *   Open Android Studio.
    *   Select "Open an Existing Project" and choose the cloned project.
    *   Wait for the necessary Gradle synchronizations to complete.
4.  **Run the Application:**
    *   Select an emulator or a physical Android device.
    *   Run the "Run 'app'" command from Android Studio.

## Test User Credentials

You can use the following credentials to quickly test the application:

*   **Email:** `atilsamancioglu@test.com`
*   **Password:** `Test123`

## Screenshots

![image](https://github.com/user-attachments/assets/bc83783f-73d5-4e95-b739-c7ea91db0ca7)
![image](https://github.com/user-attachments/assets/62a397e0-d0c6-4045-8e41-96fac6ca31e5)
![image](https://github.com/user-attachments/assets/ac5d9327-1aa8-4b5a-a2db-99f94b586b29)
