# WarungKu - Smart POS System

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://codemagic.io)
[![AdMob](https://img.shields.io/badge/AdMob-integrated-blue)](https://admob.google.com)
[![Android](https://img.shields.io/badge/platform-Android-green)](https://developer.android.com)

**WarungKu** is a comprehensive Point-of-Sale (POS) system designed for small to medium businesses in Indonesia. Built with modern Android architecture and integrated with AdMob monetization.

## 🚀 Features

### 💰 **Monetization**
- **AdMob Integration**: Banner and interstitial ads with smart frequency control
- **Revenue Analytics**: Comprehensive ad performance tracking
- **Privacy Compliant**: GDPR and child safety features

### 📱 **Core Functionality**
- **Inventory Management**: Stock tracking with barcode scanning
- **Sales System**: Complete POS with cart and checkout
- **Financial Reports**: Profit tracking and business analytics
- **Payment Proof**: QRIS receipt management
- **Multi-user Support**: Role-based access control
- **Offline Mode**: Local SQLite with cloud synchronization

### 🔧 **Technical Features**
- **Modern Architecture**: MVVM with Room database
- **Material Design**: Consistent and intuitive UI
- **Camera Integration**: ML Kit barcode scanning
- **Cloud Backend**: Supabase integration
- **Automated Builds**: Codemagic CI/CD pipeline

## 📋 Quick Start

### **Development Setup**
```bash
# Clone the repository
git clone <repository-url>
cd warungku

# Build and run on emulator
./run_app_with_emulator.sh

# Or launch emulator separately
./launch_emulator_with_webcam.sh
```

### **Production Build**
```bash
# Clean and build release
./cleanup_project.sh
./gradlew assembleRelease
```

## 🎯 AdMob Configuration

### **Automatic Configuration**
- **Debug builds** → Test ads (safe for development)
- **Release builds** → Production ads (real revenue)

### **Manual Configuration**
```java
AdConfigManager configManager = new AdConfigManager(context);
configManager.switchToProductionConfig(); // Force production ads
configManager.useAutomaticConfig();       // Use automatic detection
```

### **Setup Production Ads**
1. Edit `app/src/main/assets/ads_config_production.json`
2. Replace placeholder IDs with your AdMob ad unit IDs
3. Build and deploy

## 🏗️ Build & Deploy

### **Codemagic CI/CD**
- Automated builds with AdMob validation
- Release builds automatically use production ads
- AAB and APK generation for Play Store

### **Environment Variables**
Set in Codemagic UI:
- `CM_KEYSTORE` - Base64 encoded keystore
- `ADMOB_APP_ID` - AdMob application ID
- `SUPABASE_URL` - Backend URL
- `EMAIL_RECIPIENT` - Build notifications

## 📁 Project Structure

```
warungku/
├── app/                    # Android application
│   ├── src/main/assets/   # AdMob JSON configurations
│   └── src/main/java/     # Application source code
├── docs/                  # Documentation
│   ├── admob/            # AdMob setup guides
│   ├── codemagic/        # CI/CD configuration
│   └── guides/           # Setup and testing guides
├── codemagic.yaml        # CI/CD pipeline
└── *.sh                  # Development scripts
```

## 🔧 Development Scripts

- `cleanup_project.sh` - Clean build artifacts
- `run_app_with_emulator.sh` - Build and run app
- `launch_emulator_with_webcam.sh` - Start emulator with camera
- `replace_app_icon.sh` - Update app icon

## 📊 Monetization Strategy

### **Revenue Optimization**
- **Banner Ads**: Continuous revenue on all pages
- **Interstitial Ads**: 4 per hour with 15-minute cooldown
- **Smart Targeting**: Privacy-compliant personalization
- **UX Protection**: Non-intrusive ad placement

### **Performance Metrics**
- High fill rates with Google AdMob
- Balanced user experience and revenue
- Comprehensive analytics and reporting

## 🎯 Production Ready

### **✅ Completed**
- Complete AdMob monetization system
- Flexible JSON-based configuration
- Automated CI/CD pipeline
- Comprehensive documentation
- Production-optimized codebase

### **📝 Next Steps**
1. Update AdMob production configuration
2. Configure Codemagic environment variables
3. Trigger production build
4. Deploy to Google Play Store

## 📖 Documentation

Comprehensive documentation available in `/docs/`:
- **AdMob Setup**: Configuration and monetization guides
- **Codemagic Integration**: CI/CD pipeline setup
- **Development Guides**: Setup and testing instructions
- **Implementation Summaries**: Feature development details

## 🤝 Contributing

This is a production-ready commercial application. For development:

1. Follow the setup instructions
2. Use debug builds for development (automatic test ads)
3. Test production configuration before release
4. Maintain code quality and documentation

## 📄 License

Commercial application - All rights reserved.

## 🎉 Status

**Production Ready** - Complete implementation with full feature set and monetization system.

---

**Built with ❤️ for Indonesian small businesses**