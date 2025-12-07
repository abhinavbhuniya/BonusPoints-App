# Contributing to BonusPoints

Thank you for your interest in contributing to **BonusPoints**!  
This project follows strong security and quality guidelines, so please read this document before contributing.

---

## 🧰 How to Contribute

You can contribute in several ways:
- Fix bugs  
- Add new features  
- Improve UI/UX  
- Strengthen security (hashing, validation, RBAC logic)  
- Improve documentation  
- Add performance optimizations  
- Add test cases  

---

## 📂 Project Setup

Follow these steps to set up the project locally:

### 1. Fork the repository
Click the **Fork** button on GitHub.

### 2. Clone your fork
Replace `<your-username>` with your GitHub username — for you, it is already filled:

```bash
git clone https://github.com/abhinavbhuniya/BonusPoints-App.git
```

### 3. Open the project in Android Studio
- Select **File → Open**
- Choose the cloned folder  
- Let Gradle sync

### 4. Create a new branch
```bash
git checkout -b feature/<your-feature-name>
```

Examples:
```
feature/add-dark-mode
feature/improve-security
bugfix/fix-login-crash
```

---

## 📏 Coding Standards

Please follow these rules when contributing:

- Follow Java & Android best practices  
- Use `camelCase` for methods & variables  
- Use `PascalCase` for classes  
- Add comments for non-trivial logic  
- Do *NOT* hardcode credentials or sensitive data  
- All database queries must use **parameterized queries**  
- Maintain the RBAC & security model  
- Avoid unnecessary logs in production builds  

---

## 📝 Commit Message Guidelines

Use clear, structured commit messages:

```
feat: add faculty subject filtering
fix: resolve crash when awarding points
docs: update README with screenshots
refactor: improve SQLite helper structure
perf: optimize RecyclerView loading
test: add unit tests for login module
```

---

## 🔐 Security Requirements

Since BonusPoints is a **security-focused app**, all contributions must follow strict security guidelines:

### ✔ Password Handling
- Never store plaintext passwords  
- Always use SHA-256 with salt  
- No logging of sensitive fields  

### ✔ Database & SQL Requirements
- Only use parameterized queries  
- No dynamic SQL  
- Student data must remain isolated by subject (RBAC rules)  

### ✔ Authentication Rules
- No bypass of admin setup  
- Sessions must be validated correctly  
- No insecure SharedPreferences usage  

---

## 🧪 Testing Requirements

Before submitting:
- Ensure project builds successfully  
- Test on a real device or emulator  
- Validate login, RBAC, CRUD operations  
- Check that no security rules are broken  
- Verify no crashes are introduced  

---

## 📤 Submitting a Pull Request (PR)

When your feature or fix is ready:

1. Ensure your fork is up to date  
2. Push your branch  
   ```bash
   git push origin feature/<your-feature-name>
   ```
3. Create a Pull Request on GitHub  
4. Provide:
   - Clear description of the change  
   - Screenshots (if UI-related)  
   - Issue number (if fixing an existing issue)  
5. Maintainers will review your PR  

---

## 🎉 Thank You!

Your contributions help improve the security, reliability, and usefulness of BonusPoints for academic institutions.

We appreciate your effort and welcome your ideas!
