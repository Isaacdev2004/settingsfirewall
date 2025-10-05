"""
License Management System - Main Flask Application
Handles license activation, validation, and admin management
"""

import os
import secrets
from datetime import datetime, timedelta
from functools import wraps

import jwt
from flask import Flask, request, jsonify, render_template, redirect, url_for, flash, session
from flask_login import LoginManager, login_user, logout_user, login_required, current_user
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
from flask_wtf.csrf import CSRFProtect
from werkzeug.security import generate_password_hash, check_password_hash
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Initialize Flask app
app = Flask(__name__)
app.config['SECRET_KEY'] = os.getenv('SECRET_KEY', secrets.token_hex(32))
app.config['SQLALCHEMY_DATABASE_URI'] = os.getenv('DATABASE_URL', 'sqlite:///license_system.db')
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['JWT_SECRET_KEY'] = os.getenv('JWT_SECRET_KEY', secrets.token_hex(32))
app.config['JWT_ACCESS_TOKEN_EXPIRES'] = timedelta(hours=24)

# Initialize extensions
db = SQLAlchemy(app)
migrate = Migrate(app, db)
login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'admin_login'
csrf = CSRFProtect(app)

# Import models after db initialization
from models import AdminUser, License, Device, AuditLog

@login_manager.user_loader
def load_user(user_id):
    return AdminUser.query.get(int(user_id))

# JWT Authentication decorator
def jwt_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('Authorization')
        if not token:
            return jsonify({'error': 'Token is missing'}), 401
        
        try:
            if token.startswith('Bearer '):
                token = token[7:]
            data = jwt.decode(token, app.config['JWT_SECRET_KEY'], algorithms=['HS256'])
            current_user_id = data['user_id']
        except jwt.ExpiredSignatureError:
            return jsonify({'error': 'Token has expired'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'error': 'Token is invalid'}), 401
        
        return f(current_user_id, *args, **kwargs)
    return decorated

# API Routes

@app.route('/activate', methods=['POST'])
def activate_license():
    """
    Activate a license for a device
    Expected JSON: {"license_key": "string", "device_id": "string", "device_info": "string"}
    """
    try:
        data = request.get_json()
        if not data or not all(k in data for k in ['license_key', 'device_id']):
            return jsonify({'error': 'Missing required fields'}), 400
        
        license_key = data['license_key']
        device_id = data['device_id']
        device_info = data.get('device_info', '')
        
        # Find license
        license_obj = License.query.filter_by(key=license_key).first()
        if not license_obj:
            return jsonify({'error': 'Invalid license key'}), 404
        
        # Check if license is active
        if license_obj.status != 'active':
            return jsonify({'error': 'License is not active'}), 400
        
        # Check if license is expired
        if license_obj.expires_at and license_obj.expires_at < datetime.utcnow():
            license_obj.status = 'expired'
            db.session.commit()
            return jsonify({'error': 'License has expired'}), 400
        
        # Check if device is already registered
        existing_device = Device.query.filter_by(device_id=device_id).first()
        if existing_device:
            if existing_device.license_id == license_obj.id:
                # Device already registered with this license, return existing token
                token = jwt.encode({
                    'user_id': device_id,
                    'license_id': license_obj.id,
                    'exp': datetime.utcnow() + app.config['JWT_ACCESS_TOKEN_EXPIRES']
                }, app.config['JWT_SECRET_KEY'], algorithm='HS256')
                
                return jsonify({
                    'success': True,
                    'token': token,
                    'license_status': license_obj.status,
                    'expires_at': license_obj.expires_at.isoformat() if license_obj.expires_at else None
                })
            else:
                return jsonify({'error': 'Device already registered with different license'}), 400
        
        # Register new device
        device = Device(
            device_id=device_id,
            license_id=license_obj.id,
            device_info=device_info,
            registered_at=datetime.utcnow()
        )
        db.session.add(device)
        
        # Log activation
        audit_log = AuditLog(
            action='license_activated',
            license_id=license_obj.id,
            device_id=device_id,
            details=f'Device {device_id} activated license {license_key}'
        )
        db.session.add(audit_log)
        
        db.session.commit()
        
        # Generate JWT token
        token = jwt.encode({
            'user_id': device_id,
            'license_id': license_obj.id,
            'exp': datetime.utcnow() + app.config['JWT_ACCESS_TOKEN_EXPIRES']
        }, app.config['JWT_SECRET_KEY'], algorithm='HS256')
        
        return jsonify({
            'success': True,
            'token': token,
            'license_status': license_obj.status,
            'expires_at': license_obj.expires_at.isoformat() if license_obj.expires_at else None
        })
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/validate', methods=['POST'])
@jwt_required
def validate_license(user_id):
    """
    Validate a license for a device
    Requires JWT token in Authorization header
    """
    try:
        # Find device
        device = Device.query.filter_by(device_id=user_id).first()
        if not device:
            return jsonify({'error': 'Device not found'}), 404
        
        # Find license
        license_obj = License.query.get(device.license_id)
        if not license_obj:
            return jsonify({'error': 'License not found'}), 404
        
        # Check license status
        if license_obj.status != 'active':
            return jsonify({'error': 'License is not active', 'status': license_obj.status}), 400
        
        # Check if license is expired
        if license_obj.expires_at and license_obj.expires_at < datetime.utcnow():
            license_obj.status = 'expired'
            db.session.commit()
            return jsonify({'error': 'License has expired', 'status': 'expired'}), 400
        
        # Update last validation
        device.last_validated = datetime.utcnow()
        db.session.commit()
        
        return jsonify({
            'valid': True,
            'license_status': license_obj.status,
            'expires_at': license_obj.expires_at.isoformat() if license_obj.expires_at else None,
            'days_remaining': (license_obj.expires_at - datetime.utcnow()).days if license_obj.expires_at else None
        })
        
    except Exception as e:
        return jsonify({'error': 'Internal server error'}), 500

# Admin Routes

@app.route('/admin/login', methods=['GET', 'POST'])
def admin_login():
    """Admin login page"""
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        
        admin = AdminUser.query.filter_by(username=username).first()
        if admin and check_password_hash(admin.password_hash, password):
            login_user(admin)
            return redirect(url_for('admin_dashboard'))
        else:
            flash('Invalid username or password', 'error')
    
    return render_template('admin/login.html')

@app.route('/admin/logout')
@login_required
def admin_logout():
    """Admin logout"""
    logout_user()
    return redirect(url_for('admin_login'))

@app.route('/admin')
@login_required
def admin_dashboard():
    """Admin dashboard"""
    licenses = License.query.all()
    devices = Device.query.all()
    recent_logs = AuditLog.query.order_by(AuditLog.created_at.desc()).limit(10).all()
    
    return render_template('admin/dashboard.html', 
                         licenses=licenses, 
                         devices=devices, 
                         recent_logs=recent_logs)

@app.route('/admin/licenses')
@login_required
def admin_licenses():
    """Manage licenses"""
    licenses = License.query.all()
    return render_template('admin/licenses.html', licenses=licenses)

@app.route('/admin/licenses/create', methods=['POST'])
@login_required
def create_license():
    """Create new license"""
    try:
        key = request.form.get('key')
        duration_days = int(request.form.get('duration_days', 7))
        
        if not key:
            flash('License key is required', 'error')
            return redirect(url_for('admin_licenses'))
        
        # Check if key already exists
        if License.query.filter_by(key=key).first():
            flash('License key already exists', 'error')
            return redirect(url_for('admin_licenses'))
        
        expires_at = datetime.utcnow() + timedelta(days=duration_days)
        
        license_obj = License(
            key=key,
            status='active',
            expires_at=expires_at,
            created_by=current_user.id
        )
        db.session.add(license_obj)
        db.session.commit()
        
        flash('License created successfully', 'success')
        return redirect(url_for('admin_licenses'))
        
    except Exception as e:
        db.session.rollback()
        flash('Error creating license', 'error')
        return redirect(url_for('admin_licenses'))

@app.route('/admin/licenses/<int:license_id>/revoke', methods=['POST'])
@login_required
def revoke_license(license_id):
    """Revoke a license"""
    try:
        license_obj = License.query.get_or_404(license_id)
        license_obj.status = 'revoked'
        license_obj.revoked_at = datetime.utcnow()
        license_obj.revoked_by = current_user.id
        db.session.commit()
        
        # Log revocation
        audit_log = AuditLog(
            action='license_revoked',
            license_id=license_id,
            details=f'License {license_obj.key} revoked by admin'
        )
        db.session.add(audit_log)
        db.session.commit()
        
        flash('License revoked successfully', 'success')
        return redirect(url_for('admin_licenses'))
        
    except Exception as e:
        db.session.rollback()
        flash('Error revoking license', 'error')
        return redirect(url_for('admin_licenses'))

@app.route('/admin/devices')
@login_required
def admin_devices():
    """Manage devices"""
    devices = Device.query.join(License).all()
    return render_template('admin/devices.html', devices=devices)

@app.route('/admin/notifications', methods=['GET', 'POST'])
@login_required
def admin_notifications():
    """Send notifications to devices"""
    if request.method == 'POST':
        title = request.form.get('title')
        message = request.form.get('message')
        target = request.form.get('target', 'all')  # all, specific_license, specific_device
        
        # TODO: Implement FCM notification sending
        flash('Notification sent successfully', 'success')
        return redirect(url_for('admin_notifications'))
    
    return render_template('admin/notifications.html')

# Error handlers
@app.errorhandler(404)
def not_found(error):
    return jsonify({'error': 'Not found'}), 404

@app.errorhandler(500)
def internal_error(error):
    db.session.rollback()
    return jsonify({'error': 'Internal server error'}), 500

if __name__ == '__main__':
    with app.app_context():
        db.create_all()
        
        # Create default admin user if none exists
        if not AdminUser.query.first():
            admin = AdminUser(
                username='admin',
                email='admin@example.com',
                password_hash=generate_password_hash('admin123')
            )
            db.session.add(admin)
            db.session.commit()
            print("Default admin user created: username=admin, password=admin123")
    
    app.run(debug=True, host='0.0.0.0', port=5000)
