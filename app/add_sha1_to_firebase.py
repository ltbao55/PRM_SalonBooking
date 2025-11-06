#!/usr/bin/env python3
"""
Script để tự động thêm SHA-1 vào Firebase Console
Yêu cầu: Firebase CLI đã được cài đặt và đã login
"""

import subprocess
import sys
import os

# SHA-1 của release keystore
RELEASE_SHA1 = "86:72:A6:8A:2A:2D:36:C4:A7:4A:06:59:65:D0:3A:BA:0A:12:1F:F2"
PROJECT_ID = "prm-salonbooking"

def check_firebase_cli():
    """Kiểm tra Firebase CLI đã được cài đặt chưa"""
    try:
        result = subprocess.run(["firebase", "--version"], 
                              capture_output=True, text=True, check=True)
        print(f"✅ Firebase CLI found: {result.stdout.strip()}")
        return True
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("❌ Firebase CLI not found!")
        print("   Please install Firebase CLI: npm install -g firebase-tools")
        return False

def check_firebase_login():
    """Kiểm tra đã login Firebase chưa"""
    try:
        result = subprocess.run(["firebase", "projects:list"], 
                              capture_output=True, text=True, check=True)
        if PROJECT_ID in result.stdout:
            print(f"✅ Logged in to Firebase and project '{PROJECT_ID}' found")
            return True
        else:
            print(f"⚠️  Project '{PROJECT_ID}' not found in your projects")
            print("   Available projects:")
            print(result.stdout)
            return False
    except subprocess.CalledProcessError:
        print("❌ Not logged in to Firebase!")
        print("   Please run: firebase login")
        return False

def add_sha1_to_firebase():
    """Thêm SHA-1 vào Firebase"""
    print(f"\n📝 Adding SHA-1 to Firebase project: {PROJECT_ID}")
    print(f"   SHA-1: {RELEASE_SHA1}\n")
    
    try:
        # Lệnh Firebase CLI để thêm SHA-1
        # Lưu ý: Lệnh này có thể khác tùy phiên bản Firebase CLI
        # Kiểm tra: firebase apps:android:sha --help
        cmd = [
            "firebase", "apps:android:sha:create",
            PROJECT_ID,
            "--sha", RELEASE_SHA1.replace(":", "")
        ]
        
        result = subprocess.run(cmd, capture_output=True, text=True)
        
        if result.returncode == 0:
            print("✅ SHA-1 added successfully!")
            print("\n📥 Next steps:")
            print("   1. Go to Firebase Console and download updated google-services.json")
            print("   2. Replace app/google-services.json with the new file")
            print("   3. Rebuild your release APK")
            return True
        else:
            print("❌ Failed to add SHA-1:")
            print(result.stderr)
            print("\n💡 Alternative: Add SHA-1 manually in Firebase Console")
            print("   See GOOGLE_SIGNIN_FIX.md for detailed instructions")
            return False
            
    except Exception as e:
        print(f"❌ Error: {e}")
        print("\n💡 Please add SHA-1 manually in Firebase Console")
        print("   See GOOGLE_SIGNIN_FIX.md for detailed instructions")
        return False

def main():
    print("=" * 60)
    print("Firebase SHA-1 Auto-Add Script")
    print("=" * 60)
    print()
    
    # Kiểm tra Firebase CLI
    if not check_firebase_cli():
        print("\n💡 Manual method:")
        print("   See GOOGLE_SIGNIN_FIX.md for manual instructions")
        sys.exit(1)
    
    # Kiểm tra login
    if not check_firebase_login():
        print("\n💡 Please login first:")
        print("   firebase login")
        sys.exit(1)
    
    # Thêm SHA-1
    if add_sha1_to_firebase():
        print("\n✅ Done! Remember to download updated google-services.json")
    else:
        print("\n⚠️  Automatic method failed. Please use manual method.")
        print("   See GOOGLE_SIGNIN_FIX.md for detailed instructions")

if __name__ == "__main__":
    main()


