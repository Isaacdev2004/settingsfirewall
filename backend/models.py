"""
SQLAlchemy models for the License Management System
"""

from datetime import datetime
from flask_sqlalchemy import SQLAlchemy
from flask_login import UserMixin

db = SQLAlchemy()

class AdminUser(UserMixin, db.Model):
    """Admin user model for managing licenses and devices"""
    __tablename__ = 'admin_users'
    
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(255), nullable=False)
    is_active = db.Column(db.Boolean, default=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    last_login = db.Column(db.DateTime)
    
    # Relationships
    created_licenses = db.relationship('License', foreign_keys='License.created_by', backref='creator', lazy='dynamic')
    revoked_licenses = db.relationship('License', foreign_keys='License.revoked_by', backref='revoker', lazy='dynamic')
    
    def __repr__(self):
        return f'<AdminUser {self.username}>'

class License(db.Model):
    """License model for managing software licenses"""
    __tablename__ = 'licenses'
    
    id = db.Column(db.Integer, primary_key=True)
    key = db.Column(db.String(255), unique=True, nullable=False, index=True)
    status = db.Column(db.String(20), default='active', nullable=False)  # active, expired, revoked
    expires_at = db.Column(db.DateTime)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    revoked_at = db.Column(db.DateTime)
    
    # Foreign keys
    created_by = db.Column(db.Integer, db.ForeignKey('admin_users.id'))
    revoked_by = db.Column(db.Integer, db.ForeignKey('admin_users.id'))
    
    # Relationships
    devices = db.relationship('Device', backref='license', lazy='dynamic')
    audit_logs = db.relationship('AuditLog', backref='license', lazy='dynamic')
    
    def __repr__(self):
        return f'<License {self.key}>'
    
    @property
    def is_expired(self):
        """Check if license is expired"""
        if not self.expires_at:
            return False
        return datetime.utcnow() > self.expires_at
    
    @property
    def days_remaining(self):
        """Get days remaining until expiration"""
        if not self.expires_at:
            return None
        delta = self.expires_at - datetime.utcnow()
        return delta.days if delta.days > 0 else 0

class Device(db.Model):
    """Device model for tracking registered devices"""
    __tablename__ = 'devices'
    
    id = db.Column(db.Integer, primary_key=True)
    device_id = db.Column(db.String(255), nullable=False, index=True)
    device_info = db.Column(db.Text)
    fcm_token = db.Column(db.String(255))  # Firebase Cloud Messaging token
    registered_at = db.Column(db.DateTime, default=datetime.utcnow)
    last_validated = db.Column(db.DateTime)
    is_active = db.Column(db.Boolean, default=True)
    
    # Foreign keys
    license_id = db.Column(db.Integer, db.ForeignKey('licenses.id'), nullable=False)
    
    # Relationships
    audit_logs = db.relationship('AuditLog', backref='device', lazy='dynamic')
    
    def __repr__(self):
        return f'<Device {self.device_id}>'

class AuditLog(db.Model):
    """Audit log model for tracking all system activities"""
    __tablename__ = 'audit_logs'
    
    id = db.Column(db.Integer, primary_key=True)
    action = db.Column(db.String(100), nullable=False)  # license_activated, license_revoked, etc.
    details = db.Column(db.Text)
    ip_address = db.Column(db.String(45))  # IPv4 or IPv6
    user_agent = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    # Foreign keys (optional)
    license_id = db.Column(db.Integer, db.ForeignKey('licenses.id'))
    device_id = db.Column(db.Integer, db.ForeignKey('devices.id'))  # Changed to proper foreign key
    admin_user_id = db.Column(db.Integer, db.ForeignKey('admin_users.id'))
    
    # Relationships
    admin_user = db.relationship('AdminUser', backref='audit_logs')
    
    def __repr__(self):
        return f'<AuditLog {self.action} at {self.created_at}>'
