# Live 77 – Android IPTV App

A clean Android app (Kotlin) that displays a login screen and then a clickable list of IPTV channels loaded from a remote M3U playlist. Tapping a channel starts playback using Media3 / ExoPlayer.

---

## Features

| Feature | Detail |
|---|---|
| Login screen | 6-digit login + 6-digit password with input validation |
| Channel list | Loads channels from the remote M3U playlist via OkHttp, displayed in a RecyclerView |
| Playback | Streams the selected channel with Media3 ExoPlayer (HLS / HTTP) |
| Error handling | Network failures and malformed playlist data are caught and shown to the user with a retry button |

---

## Login credentials

> **Assumption:** no backend authentication server is available.  
> Credentials are validated locally with the hardcoded values below.  
> Change `validLogin` / `validPassword` in `LoginViewModel.kt` when a real auth API is ready.

| Field | Value |
|---|---|
| Login | `123456` |
| Password | `123456` |

---

## Playlist URL

```
https://ontvadmin.info85.com.br/files/lista.m3u
```

The URL is configured in `ChannelListViewModel.kt` (`PLAYLIST_URL` constant).

---

## Tech stack

- **Language:** Kotlin
- **UI:** ViewBinding, ConstraintLayout, Material Components, RecyclerView
- **Networking:** OkHttp 4
- **Playback:** AndroidX Media3 / ExoPlayer (HLS + progressive)
- **Async:** Kotlin Coroutines + ViewModel / LiveData
- **Min SDK:** 26 (Android 8.0)

---

## How to build

### Android Studio (recommended)

1. Clone the repository
2. Open the project root in **Android Studio Hedgehog (2023.1)** or newer
3. Let Gradle sync complete (Android Studio will download Gradle automatically)
4. Run on a device or emulator

### Command line

```bash
# Build a debug APK (Gradle wrapper is already included in the repository)
./gradlew assembleDebug
```

The generated APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Project structure

```
app/src/main/
├── java/com/live77/
│   ├── data/          M3uParser       – fetches & parses the remote playlist
│   ├── model/         Channel         – data class
│   └── ui/
│       ├── login/     LoginActivity + LoginViewModel
│       ├── channels/  ChannelListActivity + ChannelListViewModel + ChannelAdapter
│       └── player/    PlayerActivity  – Media3 / ExoPlayer
└── res/
    ├── layout/        activity_login, activity_channel_list, item_channel, activity_player
    └── values/        strings, colors, themes
```