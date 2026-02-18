# VolumeNav: System-Wide Volume Key Controller

Me app eka design karala thiyenne touch screen weda nathi Android 5.0 (API 21) saha eita udatha devices wala system navigation volume buttons walin karanna.

---

## 1. Technical Requirements & Architecture
* **Minimum SDK:** API 21 (Android 5.0 Lollipop).
* **Primary API:** `AccessibilityService`.
* **Performance Goal:** Low memory footprint (no heavy UI/animations).
* **Permissions:** `BIND_ACCESSIBILITY_SERVICE`.

---

## 2. Core Functionality (Logic)
Volume buttons walin system eka control karanna me widihata logic eka hadanna:

| Input Trigger | Action |
| :--- | :--- |
| **Volume Up (Single Press)** | Move Selection Up / Previous Item |
| **Volume Down (Single Press)** | Move Selection Down / Next Item |
| **Volume Up (Long Press)** | **Enter / Select** (Perform Click) |
| **Volume Down (Long Press)** | **Back** Button |
| **Volume Up + Down (Both)** | **Home** Button |

---

## 3. Implementation Steps (Technical)

### Step A: Accessibility Service Configuration
`res/xml/volume_nav_service.xml` file ekak hadala meka danna:
```xml
<accessibility-service xmlns:android="[http://schemas.android.com/apk/res/android](http://schemas.android.com/apk/res/android)"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagRequestFilterKeyEvents|flagRetrieveInteractiveWindows"
    android:canRetrieveWindowContent="true"
    android:description="@string/service_description" />


    ---

### Mita amatharawa podi advice ekak:
Meka hadaddi **D-Pad Navigation** kiyana concept eka study karanna. Android system eka naturally support karanawa Keyboard/D-pad walin control wenna. Oya karanna thiyenne Volume keys walin "Tab" key eka press wenwa wage signal ekak system ekata dena eka.