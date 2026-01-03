# Git Tags Documentation
## Version Control Milestones

## 📌 Current Tags

### v1.0.0-firebase
**Date:** $(date)
**Description:** Milestone: Firebase Implementation Complete

**Features:**
- Multi-user authentication & authorization
- Role-based access control (Owner/Manager/Cashier/Staff)
- Firestore sync (Products & Cash Flows)
- Invite employee system
- Cloud data synchronization

**Pre-Supabase migration checkpoint**

---

## 🔖 Tag Management

### View All Tags
```bash
git tag -l
```

### View Tag Details
```bash
git show v1.0.0-firebase
```

### Create New Tag
```bash
# Annotated tag (recommended)
git tag -a v1.0.1-supabase -m "Description"

# Lightweight tag
git tag v1.0.1-supabase
```

### Push Tags to Remote
```bash
# Push single tag
git push origin v1.0.0-firebase

# Push all tags
git push origin --tags
```

### Delete Tag
```bash
# Delete local tag
git tag -d v1.0.0-firebase

# Delete remote tag
git push origin --delete v1.0.0-firebase
```

### Checkout Tag
```bash
# View code at specific tag
git checkout v1.0.0-firebase

# Create branch from tag
git checkout -b branch-name v1.0.0-firebase
```

---

## 📋 Tag Naming Convention

### Format: `v{MAJOR}.{MINOR}.{PATCH}-{DESCRIPTION}`

**Examples:**
- `v1.0.0-firebase` - Firebase implementation
- `v1.1.0-supabase` - Supabase migration
- `v1.0.1-bugfix` - Bug fix release
- `v2.0.0-major` - Major version update

---

## 🎯 Recommended Tags for Future

### After Supabase Migration
```bash
git tag -a v1.1.0-supabase -m "Supabase migration complete"
```

### After Major Features
```bash
git tag -a v1.2.0-feature-name -m "Feature: [description]"
```

### Release Tags
```bash
git tag -a v1.0.0-release -m "First production release"
```

---

## 📝 Notes

- **Annotated tags** (dengan `-a`) lebih baik karena menyimpan metadata
- **Lightweight tags** hanya menyimpan commit hash
- Tags tidak otomatis push ke remote, harus push manual
- Tags bisa digunakan untuk release management

---

**Current checkpoint: v1.0.0-firebase** ✅

