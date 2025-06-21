package com.example.hudrelay

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class VietMapSignService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName != "vn.vietmap.live") return

        val rootNode = rootInActiveWindow ?: return

        val nodes = rootNode.findAccessibilityNodeInfosByViewId("vn.vietmap.live:id/speed_limit_sign")
        if (nodes.isNotEmpty()) {
            val desc = nodes[0].contentDescription?.toString() ?: ""
            val signCode = when {
                desc.contains("60") -> "60"
                desc.contains("Cấm vượt") -> "NOVR"
                else -> "NC"
            }
            BleSender.enqueue("SIGN:$signCode")
            Log.d("VietMapSignService", "Found sign: $signCode")
        } else {
            BleSender.enqueue("SIGN:OFF")
        }
    }

    override fun onInterrupt() {}
}
