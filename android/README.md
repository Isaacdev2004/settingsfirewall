# License Management System - Android App

A secure Android companion app for the License Management System. This app handles license activation, validation, and receives push notifications for license events.

## 📱 Features

- **License Activation**: Enter license key to activate software access
- **Secure Storage**: Uses EncryptedSharedPreferences for sensitive data
- **Background Validation**: Periodic license validation every 4 hours
- **Push Notifications**: Real-time notifications for license events
- **Expiry Warnings**: Local notifications when license is about to expire
- **Offline Support**: Cached license status for offline operation

## 🏗️ Architecture

- **Language**: Kotlin
- **UI**: Material Design 3 with ViewBinding
- **Networking**: Retrofit 2 with Gson
- **Storage**: EncryptedSharedPreferences
- **Background Tasks**: WorkManager
- **Notifications**: Firebase Cloud Messaging
- **Security**: JWT token validation

## 🚀 Setup Instructions

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 26+ (API Level 26)
- Firebase project with Cloud Messaging enabled

### 1. Firebase Configuration

1. **Create Firebase Project**:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project
   - Enable Cloud Messaging

2. **Add Android App**:
   - Click "Add app" and select Android
   - Enter package name: `com.systemmanager.license`
   - Download `google-services.json`
   - Place the file in `app/` directory

3. **Enable Cloud Messaging**:
   - In Firebase Console, go to "Cloud Messaging"
   - Enable the service

### 2. Project Configuration

1. **Open in Android Studio**:
   ```bash
   # Navigate to android directory
   cd android
   # Open in Android Studio
   ```

2. **Sync Gradle Files**:
   - Android Studio will automatically sync
   - Wait for sync to complete

3. **Update API Endpoint**:
   - Open `LicenseViewModel.kt`
   - Update the base URL in `createApiService()` method
   - Replace `"https://your-backend-url.com/"` with your actual backend URL

### 3. Build and Run

1. **Connect Device or Start Emulator**:
   - Connect Android device with USB debugging enabled
   - Or start an Android emulator (API 26+)

2. **Build and Install**:
   - Click "Run" in Android Studio
   - Or use command line: `./gradlew installDebug`

## 🔧 Configuration

### API Endpoint

Update the backend URL in `LicenseViewModel.kt`:

```kotlin
private fun createApiService(): LicenseApiService {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://your-backend-url.com/") // Update this
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    return retrofit.create(LicenseApiService::class.java)
}
```

### Notification Permissions

The app automatically requests notification permissions on first launch. Users can grant or deny these permissions.

### Background Validation

The app schedules periodic license validation every 4 hours using WorkManager. This ensures licenses are checked regularly even when the app is not in use.

## 📱 User Interface

### Main Screen

- **License Activation**: Enter license key to activate
- **Status Display**: Shows current license status and expiry
- **Validation Controls**: Manual validation and refresh options

### Notifications

- **License Revoked**: Immediate notification when license is revoked
- **Expiry Warnings**: Notifications when license expires soon
- **Admin Messages**: Notifications from system administrators

## 🔐 Security Features

### Encrypted Storage

The app uses `EncryptedSharedPreferences` to securely store:
- JWT tokens
- License status
- Expiry dates
- FCM tokens

### JWT Token Management

- Tokens are stored securely and automatically refreshed
- 24-hour expiration with automatic revalidation
- Secure transmission over HTTPS

### Device Identification

- Uses Android's secure device ID
- Includes device information for tracking
- Prevents license sharing between devices

## 🔄 License Flow

1. **User opens app** for the first time
2. **Enters license key** in activation form
3. **App sends activation request** to backend
4. **Backend validates license** and returns JWT token
5. **Token stored securely** on device
6. **Background validation** starts automatically
7. **Push notifications** received for license events
8. **App deactivates** when license expires or is revoked

## 🛠️ Development

### Project Structure

```
app/src/main/java/com/systemmanager/license/
├── data/
│   ├── api/           # API service interfaces
│   ├── model/         # Data models
│   └── repository/    # Data repository
├── service/           # Background services
├── ui/               # Activities and UI
├── utils/            # Utility classes
└── viewmodel/        # ViewModels
```

### Key Components

- **MainActivity**: Main UI and user interactions
- **LicenseViewModel**: Business logic and data management
- **LicenseRepository**: API communication and data storage
- **PreferenceManager**: Secure preference storage
- **LicenseFirebaseMessagingService**: Push notification handling
- **LicenseValidationWorker**: Background license validation

### Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run all tests
./gradlew check
```

### Code Style

The project follows Android Kotlin style guidelines:
- Use `camelCase` for variables and functions
- Use `PascalCase` for classes
- Follow Material Design principles
- Use ViewBinding for UI

## 📊 Monitoring and Logging

### Logging

The app includes comprehensive logging for:
- API requests and responses
- License validation results
- Push notification events
- Error conditions
- Background task execution

### Debug Information

Enable debug logging by setting log level in `build.gradle`:

```gradle
buildTypes {
    debug {
        buildConfigField "boolean", "DEBUG_LOGGING", "true"
    }
    release {
        buildConfigField "boolean", "DEBUG_LOGGING", "false"
    }
}
```

## 🚀 Production Build

### Signing Configuration

1. **Generate Keystore**:
   ```bash
   keytool -genkey -v -keystore license-app.keystore -alias license-app -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Configure Signing**:
   - Create `keystore.properties` file
   - Add keystore configuration to `build.gradle`

3. **Build Release APK**:
   ```bash
   ./gradlew assembleRelease
   ```

### ProGuard Configuration

The app includes ProGuard rules for code obfuscation and optimization in release builds.

## 🔍 Troubleshooting

### Common Issues

1. **Build Errors**:
   - Ensure `google-services.json` is in the correct location
   - Check that all dependencies are properly synced
   - Verify Android SDK version compatibility

2. **Firebase Issues**:
   - Verify Firebase project configuration
   - Check that Cloud Messaging is enabled
   - Ensure package name matches Firebase configuration

3. **API Connection Issues**:
   - Verify backend URL is correct
   - Check network connectivity
   - Ensure backend is running and accessible

4. **Notification Issues**:
   - Check notification permissions
   - Verify FCM token registration
   - Ensure Firebase credentials are correct

### Debug Steps

1. **Check Logs**:
   ```bash
   adb logcat | grep "LicenseManager"
   ```

2. **Verify FCM Token**:
   - Check logs for FCM token registration
   - Verify token is sent to backend

3. **Test API Endpoints**:
   - Use tools like Postman to test backend endpoints
   - Verify JWT token format and expiration

## 📝 Customization

### App Branding

1. **App Name**: Update in `strings.xml`
2. **App Icon**: Replace icons in `mipmap` directories
3. **Colors**: Modify `colors.xml` and `themes.xml`
4. **Package Name**: Update in `build.gradle` and manifest

### License Validation

Customize validation behavior in `LicenseValidationWorker.kt`:
- Change validation frequency
- Modify notification timing
- Add custom validation logic

### UI Customization

Modify `activity_main.xml` and related layouts:
- Change color scheme
- Update button styles
- Modify layout structure

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support and questions:

1. Check the troubleshooting section
2. Review Android Studio logs
3. Create an issue on GitHub
4. Contact the development team

---

**Note**: This Android app is designed to work with the Flask backend. Make sure the backend is properly configured and running before testing the app.
