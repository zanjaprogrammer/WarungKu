# Phase 4: Invite Employee Flow
## Sistem Invite Karyawan untuk Multi-User

## ✅ Status
- [x] Phase 1: Firebase Setup & Authentication (COMPLETE)
- [x] Phase 2: Authentication & Permission Checks (COMPLETE)
- [x] Phase 3: Firestore Sync Service (COMPLETE)
- [ ] Phase 4: Invite Employee Flow (IN PROGRESS)

---

## 🎯 Tujuan Phase 4

1. **Owner bisa invite karyawan** via email
2. **Employee registration flow** dengan invite code/link
3. **Auto-assign role** sesuai yang di-invite
4. **Auto-assign ke warung** owner
5. **UI untuk manage employees** (list, invite, remove)

---

## 📋 Fitur yang Perlu Di-Implementasi

### 1. **Manage Employees UI**
- Halaman untuk owner melihat daftar karyawan
- Button untuk invite karyawan baru
- List karyawan dengan role mereka
- Option untuk remove karyawan (owner only)

### 2. **Invite Employee Flow**
- Owner input: Email, Role (Manager/Cashier/Staff)
- Generate invite code atau send email invite
- Store invite data di Firestore

### 3. **Employee Registration**
- Employee buka link/input invite code
- Auto-fill warungId dan role
- Register dengan email/password
- Auto-assign ke warung owner

### 4. **Employee List & Management**
- View all employees di warung
- See their roles
- Remove employee (owner only)

---

## 🏗️ Arsitektur

### Firestore Collections

#### `invites/{inviteId}`
```javascript
{
  inviteId: string,
  warungId: string,
  ownerId: string,
  email: string,
  role: "manager" | "cashier" | "staff",
  status: "pending" | "accepted" | "expired",
  createdAt: timestamp,
  expiresAt: timestamp,
  acceptedAt: timestamp (nullable),
  acceptedBy: userId (nullable)
}
```

---

## 🔧 Implementation Steps

### Step 1: Create Invite Model
- `Invite.java` - Model untuk invite data

### Step 2: Create ManageEmployeesActivity
- UI untuk manage employees
- List employees
- Invite button
- Remove employee option

### Step 3: Create InviteEmployeeDialog
- Dialog untuk input email & role
- Generate invite code
- Save to Firestore

### Step 4: Update LoginActivity
- Check if user has pending invite
- Auto-assign warungId dan role saat register

### Step 5: Add Navigation
- Add menu item di Settings/Summary untuk "Manage Employees"
- Only visible untuk owner

---

## 📝 UI Components

### 1. **Manage Employees Page**
- Toolbar dengan title "Kelola Karyawan"
- FAB untuk "Invite Karyawan"
- RecyclerView untuk list employees
- Each item: Name, Email, Role, Remove button (owner only)

### 2. **Invite Employee Dialog**
- TextInputLayout untuk Email
- Spinner/Dropdown untuk Role (Manager/Cashier/Staff)
- Button "Kirim Invite"
- Show invite code (optional, untuk manual share)

### 3. **Employee List Item**
- Avatar/Icon
- Name & Email
- Role badge
- Remove button (if owner)

---

## 🔄 Flow Diagram

### Owner Invite Flow:
```
1. Owner: Settings → Manage Employees
2. Owner: Click "Invite Karyawan"
3. Owner: Input email & select role
4. System: Generate invite code
5. System: Save invite to Firestore
6. System: Send email (optional) or show invite code
7. Owner: Share invite code/link to employee
```

### Employee Registration Flow:
```
1. Employee: Receive invite code/link
2. Employee: Open app → Register
3. Employee: Input email (auto-filled if from link)
4. System: Check if email has pending invite
5. System: Auto-assign warungId & role
6. Employee: Input password & complete registration
7. System: Update invite status to "accepted"
8. Employee: Auto-login & redirect to MainActivity
```

---

## 🧪 Testing Strategy

1. **Test Owner Invite:**
   - Owner invite employee dengan email
   - Verify invite saved to Firestore
   - Verify invite code generated

2. **Test Employee Registration:**
   - Employee register dengan email yang di-invite
   - Verify auto-assign warungId & role
   - Verify invite status updated

3. **Test Employee List:**
   - Owner view employee list
   - Verify all employees shown
   - Verify roles correct

4. **Test Remove Employee:**
   - Owner remove employee
   - Verify employee removed from list
   - Verify employee cannot login (optional)

---

## 📋 Implementation Order

1. **Create Invite Model** - `Invite.java`
2. **Create ManageEmployeesActivity** - UI untuk manage
3. **Create InviteEmployeeDialog** - Dialog untuk invite
4. **Update LoginActivity** - Check invite saat register
5. **Add Navigation** - Menu item untuk access
6. **Add Firestore Operations** - Save/read invites

---

## ✅ Success Criteria

Phase 4 complete jika:
- ✅ Owner bisa invite karyawan
- ✅ Employee bisa register dengan invite
- ✅ Auto-assign warungId & role
- ✅ Employee list terlihat di Manage Employees
- ✅ Owner bisa remove employee

---

**Ready untuk implementasi!** 🚀

