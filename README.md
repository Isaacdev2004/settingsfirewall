# License Management System

A complete license management system built as a monorepo with Flask backend and Android companion app. This system provides secure license activation, validation, and management with Firebase Cloud Messaging integration for real-time notifications.

## 🏗️ Architecture

- **Backend**: Flask + SQLAlchemy + JWT Authentication
- **Database**: SQLite (MVP) / PostgreSQL/MySQL (Production)
- **Android App**: Kotlin + AndroidX + Firebase Messaging
- **Notifications**: Firebase Cloud Messaging
- **Security**: JWT tokens, encrypted storage, CSRF protection

## 📁 Project Structure

```
SettingsFirewall/
├── backend/                 # Flask backend application
│   ├── app.py              # Main Flask application
│   ├── models.py           # SQLAlchemy models
│   ├── firebase_service.py # Firebase Cloud Messaging service
│   ├── templates/          # HTML templates for admin panel
│   ├── migrations/         # Database migrations
│   ├── requirements.txt    # Python dependencies
│   ├── Dockerfile         # Docker configuration
│   ├── docker-compose.yml # Docker Compose setup
│   └── README.md          # Backend documentation
├── android/                # Android companion app
│   ├── app/               # Android application code
│   ├── build.gradle       # Android build configuration
│   └── README.md          # Android documentation
└── README.md              # This file
```

## 🚀 Quick Start

### Backend Setup

1. **Navigate to backend directory**:
   ```bash
   cd backend
   ```

2. **Create virtual environment**:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

4. **Configure environment**:
   ```bash
   cp env.example .env
   # Edit .env with your configuration
   ```

5. **Initialize database**:
   ```bash
   flask db init
   flask db migrate -m "Initial migration"
   flask db upgrade
   ```

6. **Run the application**:
   ```bash
   python app.py
   ```

The backend will be available at `http://localhost:5000`

**Default admin credentials**:
- Username: `admin`
- Password: `admin123`

### Android App Setup

1. **Open Android Studio**
2. **Import the project** from the `android/` directory
3. **Configure Firebase**:
   - Create a Firebase project
   - Add your Android app to the project
   - Download `google-services.json` and place it in `android/app/`
4. **Update API endpoint** in the app code
5. **Build and run** the application

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the backend directory with the following variables:

```env
# Flask Configuration
SECRET_KEY=your-secret-key-here-change-this-in-production
JWT_SECRET_KEY=your-jwt-secret-key-here-change-this-in-production
FLASK_ENV=production

# Database Configuration
DATABASE_URL=sqlite:///license_system.db

# Firebase Cloud Messaging
FIREBASE_CREDENTIALS_PATH=firebase-credentials.json

# Admin Configuration
ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=admin123
```

### Firebase Setup

1. **Create Firebase Project**:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project
   - Enable Cloud Messaging

2. **Generate Service Account**:
   - Go to Project Settings > Service Accounts
   - Generate new private key
   - Save as `firebase-credentials.json` in backend directory

3. **Configure Android App**:
   - Add Android app to Firebase project
   - Download `google-services.json`
   - Place in `android/app/` directory

## 📱 Features

### Backend Features

- ✅ **License Management**: Create, activate, validate, revoke licenses
- ✅ **Device Registration**: Track devices that activate licenses
- ✅ **Admin Panel**: Web-based administration interface
- ✅ **JWT Authentication**: Secure API access
- ✅ **Firebase Integration**: Push notifications
- ✅ **Audit Logging**: Complete activity tracking
- ✅ **Multi-database Support**: SQLite, PostgreSQL, MySQL

### Android App Features

- ✅ **License Activation**: Enter license key to activate
- ✅ **Secure Storage**: EncryptedSharedPreferences for sensitive data
- ✅ **Background Validation**: Periodic license validation
- ✅ **Push Notifications**: Real-time license events
- ✅ **Expiry Warnings**: Local notifications for expiring licenses
- ✅ **Offline Support**: Cached license status

## 🔐 Security Features

- **JWT Authentication**: Secure API access with 24-hour expiration
- **Encrypted Storage**: Android app uses EncryptedSharedPreferences
- **CSRF Protection**: Flask-WTF CSRF tokens
- **Password Hashing**: Werkzeug secure password hashing
- **HTTPS Ready**: Production-ready security configuration
- **Input Validation**: SQL injection prevention

## 🚀 Deployment

### Docker Deployment

```bash
# Using Docker Compose
cd backend
docker-compose up -d
```

### Railway Deployment

1. Connect GitHub repository to Railway
2. Set environment variables in Railway dashboard
3. Deploy automatically on push

### Render Deployment

1. Create new Web Service on Render
2. Connect GitHub repository
3. Set build command: `pip install -r requirements.txt`
4. Set start command: `gunicorn --bind 0.0.0.0:$PORT app:app`
5. Add environment variables

### VPS Deployment

1. Install Docker and Docker Compose on your VPS
2. Clone the repository
3. Configure environment variables
4. Run `docker-compose up -d`

## 📊 API Endpoints

### Public Endpoints

- `POST /activate` - Activate a license for a device
- `POST /validate` - Validate a license (requires JWT)

### Admin Endpoints

- `GET /admin/login` - Admin login page
- `POST /admin/login` - Admin authentication
- `GET /admin` - Admin dashboard
- `GET /admin/licenses` - License management
- `POST /admin/licenses/create` - Create new license
- `POST /admin/licenses/<id>/revoke` - Revoke license
- `GET /admin/devices` - Device management
- `GET /admin/notifications` - Send notifications

## 🔄 License Flow

1. **Admin creates license** in the admin panel
2. **User enters license key** in Android app
3. **App activates license** via `/activate` endpoint
4. **JWT token stored** securely on device
5. **Periodic validation** every 4 hours
6. **Push notifications** for license events
7. **Automatic deactivation** when license expires/revoked

## 🛠️ Development

### Backend Development

```bash
# Install development dependencies
pip install -r requirements.txt

# Run in development mode
export FLASK_ENV=development
python app.py

# Run tests
python -m pytest tests/

# Code formatting
black .
flake8 .
```

### Android Development

1. Open project in Android Studio
2. Sync Gradle files
3. Run on device or emulator
4. Use Android Studio's built-in testing tools

## 📝 License Management

### Creating Licenses

1. Login to admin panel
2. Go to "Licenses" section
3. Click "Create License"
4. Enter license key and duration
5. License is immediately active

### Managing Devices

1. View all registered devices in admin panel
2. See device information and license associations
3. Send push notifications to specific devices
4. Monitor license validation activity

### Sending Notifications

1. Go to "Notifications" section
2. Choose target (all devices, specific license, etc.)
3. Enter title and message
4. Send push notification

## 🔍 Monitoring

The system includes comprehensive logging for:

- API requests and responses
- Authentication events
- License operations
- Firebase notifications
- Database operations
- Error tracking

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection**: Check DATABASE_URL format
2. **Firebase Errors**: Verify credentials and project setup
3. **JWT Issues**: Ensure JWT_SECRET_KEY is set
4. **CORS Problems**: Configure CORS_ORIGINS for API access
5. **Android Build Issues**: Check Firebase configuration

### Logs

```bash
# Docker logs
docker-compose logs web

# Application logs
tail -f app.log
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support and questions:

1. Check the troubleshooting section
2. Review the logs
3. Create an issue on GitHub
4. Contact the development team

---

**Note**: This is a complete license management system. Make sure to:
- Change default passwords in production
- Use HTTPS in production
- Configure proper Firebase credentials
- Set up proper database backups
- Monitor system logs regularly
