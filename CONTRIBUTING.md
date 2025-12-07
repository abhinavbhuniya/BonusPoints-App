# Contributing to BonusPoints

Thank you for your interest in contributing to **BonusPoints**!  
This project follows strong security and quality guidelines, so please read this document before contributing.

---

## 🧰 How to Contribute

You can contribute in several ways:
- Fix bugs  
- Add new features  
- Improve UI/UX  
- Improve security (hashing, validation, RBAC logic)  
- Improve documentation  
- Add tests  

---

## 📂 Project Setup

1. Fork this repository  
2. Clone your fork  
   ```bash
   git clone https://github.com/<your-username>/BonusPoints-App.git
3.Open the project in Android Studio

4.Let Gradle sync

5.Create a new branch for your change:
git checkout -b feature/<your-feature>

📏 Coding Standards
Follow Java & Android best practices

Use camelCase for variables & methods

Use PascalCase for classes

Add comments for non-trivial logic

Do NOT hardcode passwords, keys, or secret values

All DB operations must use parameterized queries

Maintain the RBAC model integrity

📝 Commit Guidelines
Use structured commit messages:
feat: add new bonus calculation method
fix: correct subject filter in student list
docs: update README with screenshots
refactor: optimize SQLite helper
perf: improve RecyclerView performance

🔐 Security Requirements
Because this is a security-focused app:
No storing plaintext passwords
No leaking session tokens
All new features must pass input validation
No new SQL queries without parameterization
Do not weaken RBAC or authentication logic

📤 Submitting a Pull Request (PR)
1.Ensure your branch is up to date
2.Push your branch
git push origin feature/<your-feature>
3.Open a Pull Request
4.Clearly describe your changes
5.Attach screenshots if UI-related
6.Reference issues (if applicable)

🎉 Thank You!
Your contributions improve both the app and its security for academic institutions.
