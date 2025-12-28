#!/bin/bash
# Script untuk verify Firebase setup

echo "=== Firebase Setup Verification ==="
echo ""

# Check google-services.json
if [ -f "app/google-services.json" ]; then
    echo "✅ google-services.json exists"
    
    # Check if it's placeholder
    if grep -q "PLACEHOLDER" app/google-services.json; then
        echo "⚠️  WARNING: google-services.json is still placeholder!"
        echo "   Please download the real file from Firebase Console"
    else
        echo "✅ google-services.json looks valid (not placeholder)"
    fi
else
    echo "❌ google-services.json NOT FOUND"
    echo "   Please download from Firebase Console and place in app/"
fi

echo ""
echo "=== Checking Firebase Configuration ==="

# Check build.gradle for google-services plugin
if grep -q "com.google.gms.google-services" build.gradle; then
    echo "✅ Google Services plugin found in project build.gradle"
else
    echo "❌ Google Services plugin NOT found in project build.gradle"
fi

if grep -q "id 'com.google.gms.google-services'" app/build.gradle; then
    echo "✅ Google Services plugin found in app build.gradle"
else
    echo "❌ Google Services plugin NOT found in app build.gradle"
fi

# Check Firebase dependencies
if grep -q "firebase-bom" app/build.gradle; then
    echo "✅ Firebase dependencies found"
else
    echo "❌ Firebase dependencies NOT found"
fi

echo ""
echo "=== Next Steps ==="
echo "1. Open Firebase Console: https://console.firebase.google.com/"
echo "2. Create project (if not exists)"
echo "3. Add Android app with package: com.zanjaprogrammer.warungku"
echo "4. Download google-services.json"
echo "5. Replace app/google-services.json with downloaded file"
echo "6. Enable Authentication (Email/Password)"
echo "7. Enable Firestore Database"
echo ""
echo "See FIREBASE_SETUP_STEP_BY_STEP.md for detailed instructions"
