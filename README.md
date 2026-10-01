# Gemgala BlockMe Solver — GitHub Actions APK Build

## Phone-only setup (Android Studio is NOT needed)

1. Create/sign in to GitHub.
2. Create a new repository named `Gemgala-BlockMe-Solver`.
3. Upload the **contents of this project ZIP** into the repository.
4. Open the repository's **Actions** tab.
5. Select **Build Android APK**.
6. Tap **Run workflow** → **Run workflow**.
7. Wait for the job to finish.
8. Open the completed run and download the artifact **Gemgala-BlockMe-Solver-debug**.
9. Extract it and install `app-debug.apk` on your Android phone.

The workflow installs Android SDK 35 and uses Java 17 + Gradle 8.10 on GitHub's cloud runner.

## Note
This is a prototype tuned to the supplied Gemgala BlockMe screenshot. The exact game version may require calibration of board coordinates and piece recognition. Auto Play requires Android Accessibility permission.
