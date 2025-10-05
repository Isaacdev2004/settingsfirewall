#!/bin/bash

# License Management System - Deployment Script
echo "🚀 License Management System Deployment Script"
echo "=============================================="

# Check if we're in the right directory
if [ ! -f "backend/app.py" ]; then
    echo "❌ Error: Please run this script from the project root directory"
    exit 1
fi

echo "✅ Project structure verified"

# Create necessary directories if they don't exist
mkdir -p backend/logs
mkdir -p backend/static

echo "✅ Directories created"

# Set proper permissions
chmod +x backend/app.py

echo "✅ Permissions set"

echo ""
echo "🎉 Project is ready for deployment!"
echo ""
echo "Next steps:"
echo "1. Push to GitHub: git add . && git commit -m 'Initial commit' && git push"
echo "2. Deploy on Render: https://render.com"
echo "3. Set environment variables in Render dashboard"
echo "4. Add PostgreSQL database in Render"
echo ""
echo "📋 Required Environment Variables for Render:"
echo "- SECRET_KEY"
echo "- JWT_SECRET_KEY" 
echo "- DATABASE_URL (will be provided by Render PostgreSQL)"
echo "- FIREBASE_CREDENTIALS_PATH"
echo ""
echo "🔗 Render Deployment Guide: See DEPLOYMENT.md"
