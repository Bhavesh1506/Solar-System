# NeuroFocus – AI-Based Dopamine Reallocation System

An offline Android application built with **Kotlin + Jetpack Compose** that detects addictive smartphone usage patterns and redirects users toward productive goals using a **rule-based behavioral scoring engine** (Explainable AI).

---

## Problem Statement

Digital addiction is a growing concern, especially among students. Excessive social media usage, late-night phone use, and rapid app switching are indicators of dopamine-seeking behavior that leads to reduced focus, disrupted sleep, and decreased productivity.

**NeuroFocus** addresses this by:
1. **Monitoring** real-time smartphone usage patterns
2. **Scoring** behavior using a weighted rule-based AI engine
3. **Intervening** when addictive patterns are detected
4. **Redirecting** users toward productive activities via gamified goals

---

## AI Logic – Explainable Rule-Based Expert System

### Why This Qualifies as AI

This system implements a **rule-based expert system**, a well-established branch of Artificial Intelligence dating back to the 1970s (MYCIN, DENDRAL). It is a form of **"Good Old-Fashioned AI" (GOFAI)** that uses human-codified knowledge to make autonomous decisions.

### Scoring Formula

```
dopamineScore = (socialMediaOpenCount × socialMediaWeight)
              + (lateNightUsageMinutes × lateNightWeight)
              + (appSwitchFrequency × appSwitchWeight)
              + (totalScreenTimeMinutes × screenTimeWeight)
```

| Signal | Default Weight | Rationale |
|--------|---------------|-----------|
| Social Media Opens | 2.0 | Each open = dopamine-seeking "check" behavior |
| Late-Night Usage | 0.5 | Correlates with compulsive use & sleep disruption |
| App Switching | 1.5 | Indicates shortened attention span |
| Screen Time | 0.2 | Broad indicator; lower weight since some is productive |

**Intervention Threshold**: Score ≥ 50 triggers a full-screen intervention overlay.

### AI Characteristics

1. **Pattern Recognition** – Identifies addictive behavior by analyzing multiple signals simultaneously
2. **Autonomous Decision Making** – Decides when to trigger interventions based on combined score
3. **Explainability (XAI)** – Every decision is traceable to specific input factors with exact weights
4. **Configurable Knowledge Base** – Weights serve as a tunable knowledge base
5. **Anomaly Detection** – 7-day moving average comparison flags unusual usage spikes (score > μ + 1σ)

---

## Architecture

**Clean Architecture (MVVM)** with clear separation of concerns:

```
┌──────────────────────────────────────────────┐
│           Presentation Layer                 │
│  Compose Screens → ViewModels → StateFlow    │
├──────────────────────────────────────────────┤
│              Domain Layer                    │
│  Use Cases → Score Engine → Anomaly Detector │
│  Repository Interfaces                       │
├──────────────────────────────────────────────┤
│               Data Layer                     │
│  Room DB → DAOs → Repository Implementations │
│  UsageStatsManager → WorkManager             │
├──────────────────────────────────────────────┤
│           Dependency Injection               │
│  Hilt Modules (Database, Repository)         │
└──────────────────────────────────────────────┘
```

### Key Components

| Component | Purpose |
|-----------|---------|
| `DopamineScoreEngine` | Core AI – computes weighted score from 4 behavioral signals |
| `AnomalyDetector` | Statistical anomaly detection (7-day moving average + std dev) |
| `UsageStatsTracker` | Queries Android's UsageStatsManager for real-time data |
| `UsageTrackingWorker` | Background periodic data collection via WorkManager |
| `InterventionScreen` | Full-screen overlay with contextual suggestions |
| `FocusTimerScreen` | Gamified focus sessions with XP, levels, and streaks |

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Kotlin | Programming language |
| Jetpack Compose | Modern declarative UI toolkit |
| Material 3 | Design system |
| Room | Local SQLite database |
| Hilt | Dependency injection |
| WorkManager | Background periodic usage tracking |
| UsageStatsManager | Android system API for app usage data |
| Compose Canvas | Custom chart drawing |

---

## Features

- **Dashboard** – Animated dopamine score gauge with color-coded status
- **Score Breakdown** – Explainable AI showing exactly which factors contribute
- **7-Day Analytics** – Score trends, screen time charts, intervention frequency
- **Goals & Rewards** – CRUD goals, focus sessions, XP, levels, streaks
- **Focus Timer** – Countdown timer with circular ring animation and XP rewards
- **Interventions** – Contextual suggestions (focus session, breathing, micro-goals)
- **Settings** – Configurable weights, threshold, sample data generator
- **Anomaly Detection** – Automatic flagging of unusual usage spikes
- **Fully Offline** – All data stored locally via Room, no internet required

---

## Setup & Running

1. Open the project in **Android Studio** (Hedgehog or newer)
2. Sync Gradle dependencies
3. Run on an emulator or physical device (API 26+)
4. Grant **Usage Stats** permission when prompted
5. Go to **Settings → Generate Sample Data** for demo

---

## Project Structure

```
app/src/main/java/com/neurofocus/app/
├── data/
│   ├── local/
│   │   ├── dao/           # Room DAOs (6)
│   │   ├── entity/        # Room entities (6)
│   │   └── NeuroFocusDatabase.kt
│   ├── repository/        # Repository implementations (5)
│   └── usage/             # UsageStatsTracker, WorkManager worker
├── di/                    # Hilt modules (Database, Repository)
├── domain/
│   ├── engine/            # DopamineScoreEngine, AnomalyDetector
│   ├── model/             # ScoreResult, ScoringWeights
│   ├── repository/        # Repository interfaces (5)
│   └── usecase/           # Use cases (5)
├── presentation/
│   ├── analytics/         # AnalyticsScreen + ViewModel
│   ├── dashboard/         # DashboardScreen + ViewModel
│   ├── goals/             # GoalsScreen, FocusTimerScreen + ViewModel
│   ├── intervention/      # InterventionScreen
│   ├── navigation/        # NavGraph + Bottom Navigation
│   ├── settings/          # SettingsScreen + ViewModel
│   └── theme/             # Color, Theme (Material 3)
├── MainActivity.kt
└── NeuroFocusApp.kt       # Application class (@HiltAndroidApp)
```
