# VedaNet

VedaNet is an open-source Android VPN and proxy client built with Kotlin and Jetpack Compose.

## Phase 1 MVP

- Material 3 Compose interface
- Local VPN/proxy profile storage
- VLESS, VMess, Shadowsocks and Trojan profile fields
- Connection screen and profile management
- Xray integration foundation
- GitHub Actions debug-APK build workflow

## Build

```bash
./gradlew :app:assembleDebug
```

## Security

VedaNet stores profiles only on the device. It does not include third-party servers, subscription lists, telemetry, or remote configuration.

## License

MIT