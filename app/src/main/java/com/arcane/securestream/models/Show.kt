package com.arcane.securestream.models

import com.arcane.securestream.adapters.AppAdapter

sealed interface Show : AppAdapter.Item {
    var isFavorite: Boolean
}
