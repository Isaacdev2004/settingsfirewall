# Deployment Guide - License Management System

This guide provides step-by-step instructions for deploying the License Management System to various platforms.

## 🚀 Quick Deployment Options

### 1. Railway (Recommended for Beginners)

Railway provides the easiest deployment experience with automatic builds and deployments.

#### Steps:

1. **Create Railway Account**:
   - Go to [Railway](https://railway.app/)
   - Sign up with GitHub

2. **Connect Repository**:
   - Click "New Project"
   - Select "Deploy from GitHub repo"
   - Choose your repository

3. **Configure Environment Variables**:
   ```env
   SECRET_KEY=your-secret-key-here
   JWT_SECRET_KEY=your-jwt-secret-key-here
   DATABASE_URL=postgresql://postgres:password@postgres:5432/railway
   FIREBASE_CREDENTIALS_PATH=/app/firebase-credentials.json
   ```

4. **Add PostgreSQL Database**:
   - In Railway dashboard, click "New"
   - Select "Database" > "PostgreSQL"
   - Railway will automatically set `DATABASE_URL`

5. **Deploy**:
   - Railway will automatically build and deploy
   - Your app will be available at the provided URL

### 2. Render

Render offers free hosting with automatic SSL certificates.

#### Steps:

1. **Create Render Account**:
   - Go to [Render](https://render.com/)
   - Sign up with GitHub

2. **Create Web Service**:
   - Click "New" > "Web Service"
   - Connect your GitHub repository
   - Select the repository

3. **Configure Service**:
   ```
   Name: license-management-system
   Environment: Python 3
   Build Command: pip install -r requirements.txt
   Start Command: gunicorn --bind 0.0.0.0:$PORT app:app
   ```

4. **Add Environment Variables**:
   ```env
   SECRET_KEY=your-secret-key-here
   JWT_SECRET_KEY=your-jwt-secret-key-here
   DATABASE_URL=postgresql://user:password@host:port/database
   FIREBASE_CREDENTIALS_PATH=/opt/render/project/src/firebase-credentials.json
   ```

5. **Add PostgreSQL Database**:
   - Create new PostgreSQL database in Render
   - Copy the connection string to `DATABASE_URL`

6. **Deploy**:
   - Click "Create Web Service"
   - Render will build and deploy automatically

### 3. VPS Deployment (Advanced)

For full control and customization, deploy to a VPS.

#### Prerequisites:

- Ubuntu 20.04+ VPS
- Domain name (optional)
- SSH access to VPS

#### Steps:

1. **Connect to VPS**:
   ```bash
   ssh root@your-vps-ip
   ```

2. **Update System**:
   ```bash
   apt update && apt upgrade -y
   ```

3. **Install Docker**:
   ```bash
   curl -fsSL https://get.docker.com -o get-docker.sh
   sh get-docker.sh
   ```

4. **Install Docker Compose**:
   ```bash
   curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
   chmod +x /usr/local/bin/docker-compose
   ```

5. **Clone Repository**:
   ```bash
   git clone https://github.com/your-username/license-management-system.git
   cd license-management-system/backend
   ```

6. **Configure Environment**:
   ```bash
   cp env.example .env
   nano .env
   ```

7. **Add Firebase Credentials**:
   ```bash
   # Upload your firebase-credentials.json file
   scp firebase-credentials.json root@your-vps-ip:/root/license-management-system/backend/
   ```

8. **Deploy with Docker Compose**:
   ```bash
   docker-compose up -d
   ```

9. **Setup Nginx (Optional)**:
   ```bash
   apt install nginx -y
   # Configure nginx reverse proxy
   ```

10. **Setup SSL with Let's Encrypt**:
    ```bash
    apt install certbot python3-certbot-nginx -y
    certbot --nginx -d your-domain.com
    ```

## 🔧 Environment Configuration

### Required Environment Variables

Create a `.env` file with the following variables:

```env
# Flask Configuration
SECRET_KEY=your-secret-key-here-change-this-in-production
JWT_SECRET_KEY=your-jwt-secret-key-here-change-this-in-production
FLASK_ENV=production

# Database Configuration
DATABASE_URL=postgresql://username:password@localhost:5432/license_system

# Firebase Cloud Messaging
FIREBASE_CREDENTIALS_PATH=firebase-credentials.json

# Admin Configuration
ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=your-secure-password

# Security
CSRF_SECRET_KEY=your-csrf-secret-key-here

# Server Configuration
HOST=0.0.0.0
PORT=5000
WORKERS=4
```

### Generating Secure Keys

```bash
# Generate SECRET_KEY
python -c "import secrets; print(secrets.token_hex(32))"

# Generate JWT_SECRET_KEY
python -c "import secrets; print(secrets.token_hex(32))"

# Generate CSRF_SECRET_KEY
python -c "import secrets; print(secrets.token_hex(32))"
```

## 🔥 Firebase Configuration

### 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project"
3. Enter project name: "License Management System"
4. Enable Google Analytics (optional)
5. Create project

### 2. Enable Cloud Messaging

1. In Firebase Console, go to "Cloud Messaging"
2. Click "Get started"
3. Cloud Messaging is now enabled

### 3. Generate Service Account

1. Go to Project Settings > Service Accounts
2. Click "Generate new private key"
3. Download the JSON file
4. Rename to `firebase-credentials.json`
5. Upload to your deployment

### 4. Configure Android App

1. In Firebase Console, click "Add app" > Android
2. Enter package name: `com.systemmanager.license`
3. Download `google-services.json`
4. Place in `android/app/` directory

## 🗄️ Database Setup

### PostgreSQL (Recommended for Production)

#### Using Docker:

```bash
docker run --name postgres-db \
  -e POSTGRES_DB=license_system \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=your-password \
  -p 5432:5432 \
  -d postgres:15-alpine
```

#### Using Docker Compose:

```yaml
version: '3.8'
services:
  db:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=license_system
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

volumes:
  postgres_data:
```

### MySQL Alternative

```yaml
version: '3.8'
services:
  db:
    image: mysql:8.0
    environment:
      - MYSQL_DATABASE=license_system
      - MYSQL_USER=mysql
      - MYSQL_PASSWORD=password
      - MYSQL_ROOT_PASSWORD=rootpassword
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"

volumes:
  mysql_data:
```

## 🔒 Security Configuration

### 1. HTTPS Setup

#### Using Let's Encrypt:

```bash
# Install Certbot
apt install certbot python3-certbot-nginx -y

# Get SSL certificate
certbot --nginx -d your-domain.com

# Auto-renewal
crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

#### Using Cloudflare:

1. Add your domain to Cloudflare
2. Update nameservers
3. Enable SSL/TLS encryption
4. Set SSL mode to "Full (strict)"

### 2. Firewall Configuration

```bash
# UFW (Ubuntu)
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable

# Or iptables
iptables -A INPUT -p tcp --dport 22 -j ACCEPT
iptables -A INPUT -p tcp --dport 80 -j ACCEPT
iptables -A INPUT -p tcp --dport 443 -j ACCEPT
iptables -A INPUT -j DROP
```

### 3. Environment Security

```bash
# Set proper file permissions
chmod 600 .env
chmod 600 firebase-credentials.json

# Create non-root user
adduser deploy
usermod -aG docker deploy
su - deploy
```

## 📊 Monitoring and Logging

### 1. Application Logs

```bash
# Docker logs
docker-compose logs -f web

# Application logs
tail -f app.log
```

### 2. Database Monitoring

```bash
# PostgreSQL logs
docker logs postgres-db

# Database connections
psql -h localhost -U postgres -d license_system -c "SELECT * FROM pg_stat_activity;"
```

### 3. System Monitoring

```bash
# Install monitoring tools
apt install htop iotop nethogs -y

# Monitor resources
htop
iotop
nethogs
```

## 🔄 Backup and Recovery

### 1. Database Backup

```bash
# PostgreSQL backup
pg_dump -h localhost -U postgres license_system > backup_$(date +%Y%m%d).sql

# Restore
psql -h localhost -U postgres license_system < backup_20240101.sql
```

### 2. Application Backup

```bash
# Backup application files
tar -czf app_backup_$(date +%Y%m%d).tar.gz /path/to/app

# Backup environment files
cp .env .env.backup
cp firebase-credentials.json firebase-credentials.json.backup
```

### 3. Automated Backups

```bash
# Create backup script
cat > backup.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d)
pg_dump -h localhost -U postgres license_system > /backups/db_$DATE.sql
tar -czf /backups/app_$DATE.tar.gz /path/to/app
find /backups -name "*.sql" -mtime +7 -delete
find /backups -name "*.tar.gz" -mtime +7 -delete
EOF

chmod +x backup.sh

# Add to crontab
crontab -e
# Add: 0 2 * * * /path/to/backup.sh
```

## 🚨 Troubleshooting

### Common Issues

1. **Database Connection Failed**:
   ```bash
   # Check database status
   docker ps | grep postgres
   
   # Check connection
   psql -h localhost -U postgres -d license_system
   ```

2. **Firebase Authentication Failed**:
   ```bash
   # Check credentials file
   ls -la firebase-credentials.json
   
   # Verify JSON format
   python -c "import json; json.load(open('firebase-credentials.json'))"
   ```

3. **Application Won't Start**:
   ```bash
   # Check logs
   docker-compose logs web
   
   # Check environment variables
   docker-compose exec web env
   ```

4. **SSL Certificate Issues**:
   ```bash
   # Check certificate status
   certbot certificates
   
   # Renew certificate
   certbot renew
   ```

### Performance Optimization

1. **Database Optimization**:
   ```sql
   -- Add indexes
   CREATE INDEX idx_licenses_key ON licenses(key);
   CREATE INDEX idx_devices_device_id ON devices(device_id);
   CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
   ```

2. **Application Optimization**:
   ```bash
   # Increase worker processes
   gunicorn --workers 8 --bind 0.0.0.0:5000 app:app
   
   # Use Redis for caching
   docker run -d --name redis -p 6379:6379 redis:alpine
   ```

3. **Nginx Optimization**:
   ```nginx
   # Add to nginx.conf
   gzip on;
   gzip_types text/plain application/json application/javascript text/css;
   
   # Add caching
   location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
       expires 1y;
       add_header Cache-Control "public, immutable";
   }
   ```

## 📈 Scaling

### Horizontal Scaling

1. **Load Balancer**:
   ```nginx
   upstream backend {
       server 127.0.0.1:5000;
       server 127.0.0.1:5001;
       server 127.0.0.1:5002;
   }
   
   server {
       location / {
           proxy_pass http://backend;
       }
   }
   ```

2. **Database Replication**:
   ```yaml
   # Master-slave setup
   version: '3.8'
   services:
     postgres-master:
       image: postgres:15-alpine
       environment:
         - POSTGRES_DB=license_system
         - POSTGRES_USER=postgres
         - POSTGRES_PASSWORD=password
     
     postgres-slave:
       image: postgres:15-alpine
       environment:
         - POSTGRES_DB=license_system
         - POSTGRES_USER=postgres
         - POSTGRES_PASSWORD=password
   ```

### Vertical Scaling

1. **Increase Resources**:
   - Add more CPU cores
   - Increase RAM
   - Use SSD storage

2. **Optimize Configuration**:
   ```bash
   # Increase database connections
   max_connections = 200
   
   # Increase worker processes
   workers = 8
   ```

## 🎯 Production Checklist

- [ ] Environment variables configured
- [ ] Database properly set up
- [ ] Firebase credentials uploaded
- [ ] SSL certificate installed
- [ ] Firewall configured
- [ ] Monitoring set up
- [ ] Backups configured
- [ ] Admin password changed
- [ ] Security headers configured
- [ ] Rate limiting enabled
- [ ] Log rotation configured
- [ ] Health checks implemented

## 📞 Support

For deployment support:

1. Check the troubleshooting section
2. Review application logs
3. Create an issue on GitHub
4. Contact the development team

---

**Note**: This deployment guide covers the most common scenarios. Adjust configurations based on your specific requirements and infrastructure.
