# Security policy

Mantiq is an input method and therefore handles sensitive user input. Security and privacy regressions are release blockers.

## Baseline rules

- Never commit API keys, signing keys, passwords, or service-role credentials.
- Never log user input, selected text, snippets, or clipboard contents.
- Disable learning, snippet expansion, clipboard capture, and network AI in password fields.
- Keep the keyboard usable offline.
- Route online AI through an authenticated backend with rate limiting; never embed provider secrets in the APK.
- Encrypt sensitive local records with keys protected by Android Keystore before cloud sync is added.

## Reporting

Open a private security advisory in the GitHub repository rather than a public issue when a vulnerability could expose user data.

