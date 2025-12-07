# Security Policy

## 🔐 Supported Versions

Only the latest release version receives security updates.

| Version | Supported |
|--------|-----------|
| v1.0.0 | ✅ Yes |
| <1.0.0 | ❌ No |

---

## 📢 Reporting a Vulnerability

If you find a security vulnerability, please **do NOT open a public issue**.

Instead, email the maintainer privately:

```
abhinavbhuniya@users.noreply.github.com
```

Include:
- Description of vulnerability  
- Steps to reproduce  
- Potential impact  
- Suggested fix (optional)  

---

## 🔒 Security Requirements

### ✔ Password Handling
- Never store plaintext passwords  
- Always use SHA-256 with salt  
- No logging sensitive fields  

### ✔ SQL & Data Protection
- Only use parameterized queries  
- No dynamic SQL  
- Subject-wise isolation must be preserved  

### ✔ Authentication Rules
- No bypass of admin setup  
- Secure session management  
- No insecure SharedPreferences usage  

---

## 🛡 Responsible Disclosure

We take security seriously.  
Expect a response within **48 hours** for acknowledgement and **5–7 days** for fixes.
