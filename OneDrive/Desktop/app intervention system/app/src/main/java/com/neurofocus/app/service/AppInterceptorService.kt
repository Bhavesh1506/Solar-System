package com.neurofocus.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * AppInterceptorService – AccessibilityService implementing the XP-only
 * "session bypass" model for app blocking.
 *
 * FLOW:
 * 1. User opens a blocked app (e.g., Instagram)
 * 2. Service detects TYPE_WINDOW_STATE_CHANGED → package is blocked
 * 3. Checks the in-memory [bypassedPackage] flag
 * 4. If NOT bypassed → launches InterceptActivity ("Pay 50 XP to Open")
 * 5. User pays XP → InterceptActivity sets [bypassedPackage] and finishes
 * 6. User is now in the app — service sees the same package, sees bypass → allows
 * 7. User switches to ANY other app or home screen → bypass is cleared
 * 8. Next time they open the blocked app → must pay again
 *
 * WHY IN-MEMORY (not Room/SharedPrefs):
 * - Bypass is inherently transient — only valid for the current session
 * - No persistence needed; if the service restarts, bypass should be cleared
 * - Avoids async DB queries on every accessibility event (performance)
 */
class AppInterceptorService : AccessibilityService() {

    companion object {
        /**
         * The currently bypassed package. Set by InterceptActivity after
         * successful XP payment. Cleared when the user navigates away.
         *
         * Volatile ensures visibility across threads (the Accessibility
         * callbacks and InterceptActivity run on different threads).
         */
        @Volatile
        var bypassedPackage: String? = null
    }

    /** Packages that require XP payment to open */
    private val blockedPackages = setOf(
        // ── Social Media ────────────────────────────────────────
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.twitter.android",
        "com.twitter.android.lite",
        "com.snapchat.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.reddit.frontpage",
        "com.google.android.youtube",
        "com.pinterest",
        "com.discord",
        "com.tumblr",

        // ── Games ───────────────────────────────────────────────
        "com.dts.freefireth",          // Free Fire
        "com.dts.freefiremax",         // Free Fire MAX
        "com.sticksports.stickcricket2", // Stick Cricket 2
        "com.sticksports.stickcricketsuper", // Stick Cricket Super
        "com.firsttouchgames.dls7",    // Dream League Soccer (DLS)
        "com.firsttouchgames.smp",     // Score! Match
        "com.pubg.imobile",            // BGMI
        "com.supercell.clashofclans",  // Clash of Clans
        "com.supercell.clashroyale",   // Clash Royale
        "com.kiloo.subwaysurf",        // Subway Surfers
        "com.king.candycrushsaga",     // Candy Crush Saga
        "com.miniclip.eightballpool",  // 8 Ball Pool
        "com.mojang.minecraftpe",      // Minecraft
        "com.activision.callofduty.shooter", // Call of Duty Mobile
        "com.grey.archer",                 // Archery Battle 3D
        "com.ludo.king",                   // Ludo King
        "com.innersloth.spacemafia",       // Among Us
        "com.fingersoft.hillclimb",        // Hill Climb Racing
        "com.imangi.templerun2",           // Temple Run 2
        "com.gameloft.android.ANMP.GlsAsphalt9", // Asphalt 9
        "com.tencent.ig",                  // PUBG Mobile (Global)
        "com.miHoYo.GenshinImpact",        // Genshin Impact
        "com.roblox.client",               // Roblox
        "com.scopely.stumbleguys"          // Stumble Guys
    )

    /** Debounce: prevent rapidly re-launching InterceptActivity */
    private var lastInterceptTime: Long = 0
    private val debounceMs = 2000L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // ── Ignore system packages ──────────────────────────────
        if (packageName == this.packageName ||
            packageName == "com.android.systemui" ||
            packageName.startsWith("com.android.launcher") ||
            packageName.startsWith("com.google.android.apps.nexuslauncher") ||
            packageName.startsWith("com.sec.android.app.launcher") ||
            packageName.startsWith("com.neurofocus.app")   // our own activities
        ) {
            // User went to home/system UI → clear the bypass
            bypassedPackage = null
            return
        }

        // ── Session Bypass Check ────────────────────────────────
        if (packageName == bypassedPackage) {
            // User is still in the paid app → allow through
            return
        }

        // User switched to a DIFFERENT app → clear old bypass
        if (bypassedPackage != null && packageName != bypassedPackage) {
            bypassedPackage = null
        }

        // ── Blocked List Check ──────────────────────────────────
        if (packageName !in blockedPackages) {
            return  // Not a blocked app, let it through
        }

        // ── Debounce ────────────────────────────────────────────
        val now = System.currentTimeMillis()
        if (now - lastInterceptTime < debounceMs) return
        lastInterceptTime = now

        // ── Launch Intercept Screen ─────────────────────────────
        val intent = Intent(
            this,
            Class.forName("com.neurofocus.app.presentation.intercept.InterceptActivity")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("blocked_package", packageName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // Required override — nothing needed
    }
}
