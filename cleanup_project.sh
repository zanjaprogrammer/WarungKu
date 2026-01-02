#!/bin/bash

echo "🧹 Cleaning WarungKu Project..."
echo "================================="

# Clean Gradle build files
echo "📦 Cleaning Gradle build files..."
./gradlew clean
rm -rf .gradle
rm -rf app/build
rm -rf build

# Remove APK and AAB files
echo "📱 Removing APK and AAB files..."
find . -name "*.apk" -type f -delete
find . -name "*.aab" -type f -delete

# Remove temporary files
echo "🗑️  Removing temporary files..."
find . -name "*.tmp" -type f -delete
find . -name "*.temp" -type f -delete
find . -name "*.log" -type f -delete
find . -name "*.cache" -type f -delete

# Remove IDE files
echo "💻 Cleaning IDE files..."
find . -name ".DS_Store" -type f -delete
find . -name "Thumbs.db" -type f -delete
find . -name "*.iml" -type f -delete
rm -rf .idea/workspace.xml
rm -rf .idea/tasks.xml
rm -rf .idea/gradle.xml
rm -rf .idea/assetWizardSettings.xml
rm -rf .idea/dictionaries
rm -rf .idea/libraries

# Remove local configuration files
echo "⚙️  Removing local configuration..."
rm -f local.properties
rm -f keystore.properties

# Remove compiled files
echo "🔧 Removing compiled files..."
find . -name "*.class" -type f -delete
find . -name "*.dex" -type f -delete
find . -name "*.o" -type f -delete
find . -name "*.so" -type f -delete

# Remove test artifacts
echo "🧪 Removing test artifacts..."
rm -rf app/src/test/build
rm -rf app/src/androidTest/build

# Remove lint reports
echo "📋 Removing lint reports..."
find . -name "lint-results*.xml" -type f -delete
find . -name "lint-results*.html" -type f -delete

# Remove profiler files
echo "📊 Removing profiler files..."
find . -name "*.hprof" -type f -delete
find . -name "*.trace" -type f -delete

# Clean test logs created during development
echo "📝 Removing development test files..."
rm -f history_test_logs.txt
rm -f test_*.log

echo ""
echo "✅ Project cleanup completed!"
echo ""
echo "📊 Project size after cleanup:"
du -sh . 2>/dev/null || echo "Size calculation not available"
echo ""
echo "🎯 Ready for:"
echo "  • Git commit"
echo "  • Production build"
echo "  • Distribution"
echo ""