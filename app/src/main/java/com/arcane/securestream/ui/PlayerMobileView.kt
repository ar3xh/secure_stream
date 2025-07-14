package com.arcane.securestream.ui

import android.content.Context
import android.util.AttributeSet
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView

class PlayerMobileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : PlayerView(context, attrs, defStyle) {

    val controller: PlayerControlView
        get() = PlayerView::class.java.getDeclaredField("controller").let {
            it.isAccessible = true
            it.get(this) as PlayerControlView
        }
}