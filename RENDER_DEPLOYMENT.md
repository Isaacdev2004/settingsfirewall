# Render Deployment Guide

## Quick Deploy to Render

### Step 1: Push to GitHub
```bash
# Initialize git repository
git init
git add .
git commit -m "Initial commit - License Management System"

# Create GitHub repository and push
git remote add origin https://github.com/yourusername/license-management-system.git
git branch -M main
git push -u origin main
```

### Step 2: Deploy on Render

1. **Go to Render**: https://render.com
2. **Sign up/Login** with your GitHub account
3. **Create New Web Service**:
   - Click "New" → "Web Service"
   - Connect your GitHub repository
   - Select the repository

4. **Configure Service**:
   ```
   Name: license-management-system
   Environment: Python 3
   Region: Choose closest to your users
   Branch: main
   Root Directory: backend
   Build Command: pip install -r requirements.txt
   Start Command: gunicorn --bind 0.0.0.0:$PORT app:app
   ```

5. **Add Environment Variables**:
   ```
   SECRET_KEY=your-secret-key-here
   JWT_SECRET_KEY=your-jwt-secret-key-here
   FLASK_ENV=production
   FIREBASE_CREDENTIALS_PATH=/opt/render/project/src/backend/firebase-credentials.json
   ```

6. **Add PostgreSQL Database**:
   - Click "New" → "PostgreSQL"
   - Name: license-system-db
   - Plan: Free
   - Copy the connection string to DATABASE_URL

7. **Deploy**:
   - Click "Create Web Service"
   - Render will automatically build and deploy

### Step 3: Configure Firebase

1. **Upload Firebase Credentials**:
   - In Render dashboard, go to your service
   - Go to "Environment" tab
   - Upload your `firebase-credentials.json` file
   - Or paste the JSON content as `FIREBASE_CREDENTIALS` environment variable

2. **Update Android App**:
   - Update the API endpoint in Android app
   - Replace `https://your-backend-url.com/` with your Render URL

### Step 4: Access Your App

- **Admin Panel**: `https://your-app-name.onrender.com/admin`
- **API Endpoints**: `https://your-app-name.onrender.com/activate`, `/validate`

### Default Admin Credentials
- Username: `admin`
- Password: `admin123`

**⚠️ Important**: Change the admin password after first login!

### Troubleshooting

1. **Build Fails**: Check build logs in Render dashboard
2. **Database Connection**: Verify DATABASE_URL is set correctly
3. **Firebase Issues**: Ensure credentials are uploaded properly
4. **App Not Starting**: Check start command and port configuration

### Environment Variables Reference

| Variable | Description | Required |
|----------|-------------|----------|
| `SECRET_KEY` | Flask secret key | Yes |
| `JWT_SECRET_KEY` | JWT signing key | Yes |
| `DATABASE_URL` | PostgreSQL connection string | Yes |
| `FLASK_ENV` | Flask environment | Yes |
| `FIREBASE_CREDENTIALS_PATH` | Path to Firebase credentials | No |
| `FIREBASE_CREDENTIALS` | Firebase credentials JSON | No |

### Cost
- **Free Tier**: 750 hours/month
- **PostgreSQL**: Free tier available
- **Custom Domain**: Available on paid plans

### Monitoring
- View logs in Render dashboard
- Monitor performance and errors
- Set up alerts for downtime

---

**🎉 Your License Management System is now live on Render!**
