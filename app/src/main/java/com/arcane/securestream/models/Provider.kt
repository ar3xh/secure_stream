package com.arcane.securestream.models

import com.arcane.securestream.adapters.AppAdapter

class Provider(
    val name: String,
    val logo: String,
    val language: String,

    val provider: com.arcane.securestream.providers.Provider,
) : AppAdapter.Item {


    override lateinit var itemType: AppAdapter.Type
}