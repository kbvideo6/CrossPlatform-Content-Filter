# Contributing to CrossPlatform Content Filter

Thank you for your interest in contributing to CrossPlatform Content Filter! This project aims to provide a privacy-focused, cross-platform content filtering and digital wellbeing solution for Android and Windows.

## How to Contribute

### Reporting Bugs

1. Check existing [issues](https://github.com/kbvideo6/CrossPlatform-Content-Filter/issues) to avoid duplicates
2. Use the [Bug Report template](https://github.com/kbvideo6/CrossPlatform-Content-Filter/issues/new?template=bug_report.md)
3. Include your OS version, device, and steps to reproduce

### Suggesting Features

1. Open a [Feature Request](https://github.com/kbvideo6/CrossPlatform-Content-Filter/issues/new?template=feature_request.md)
2. Describe the problem you're solving
3. Specify which platform (Android, Windows, or both)

### Pull Requests

1. **Fork** the repository
2. **Create a feature branch:** `git checkout -b feature/my-feature`
3. **Make your changes** following the code style below
4. **Test** your changes on the target platform
5. **Commit:** `git commit -m 'Add my feature'`
6. **Push:** `git push origin feature/my-feature`
7. **Open a Pull Request** using the PR template

## Code Style

### Android (Kotlin)
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use Android Studio's default formatter
- Document public functions with KDoc

### Windows (Python)
- Follow [PEP 8](https://peps.python.org/pep-0008/)
- Use type hints where possible
- Document functions with docstrings

## Project Structure

```
CrossPlatform-Content-Filter/
├── guard-ANDROID/    # Android content filter (Kotlin)
├── guard-WINDOWS/    # Windows content filter (Python)
│   ├── guard_lite/   # Lightweight blocker
│   └── ultra_guard/  # Hardened system service
└── assets/           # Screenshots and diagrams
```

## Development Setup

### Android
- Android Studio (Ladybug or newer)
- JDK 17+
- Android SDK 34

### Windows
- Python 3.10+
- Administrator privileges
- `pip install -r requirements.txt`

## Code of Conduct

Please read our [Code of Conduct](CODE_OF_CONDUCT.md) before contributing.

## Questions?

Open an issue with the `question` label or start a [Discussion](https://github.com/kbvideo6/CrossPlatform-Content-Filter/discussions).
