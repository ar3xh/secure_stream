package com.arcane.securestream.adapters.viewholders

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.arcane.securestream.R
import com.arcane.securestream.databinding.ItemProviderMobileBinding
import com.arcane.securestream.databinding.ItemProviderTvBinding
import com.arcane.securestream.models.Provider
import com.arcane.securestream.utils.UserPreferences
import com.arcane.securestream.utils.toActivity
import java.util.Locale

class ProviderViewHolder(
    private val _binding: ViewBinding
) : RecyclerView.ViewHolder(
    _binding.root
) {

    private val context = itemView.context
    private lateinit var provider: Provider

    fun bind(provider: Provider) {
        this.provider = provider

        when (_binding) {
            is ItemProviderMobileBinding -> displayMobileItem(_binding)
            is ItemProviderTvBinding -> displayTvItem(_binding)
        }
    }


    private fun displayMobileItem(binding: ItemProviderMobileBinding) {
        binding.root.apply {
            setOnClickListener {
                UserPreferences.currentProvider = provider.provider
                context.toActivity()?.apply {
                    finish()
                    startActivity(Intent(this, this::class.java))
                }
            }
        }

        Glide.with(context)
            .load(provider.logo.takeIf { it.isNotEmpty() }
                ?: R.drawable.ic_provider_default_logo)
            .fitCenter()
            .into(binding.ivProviderLogo)

        binding.tvProviderName.text = provider.name

        binding.tvProviderLanguage.text = Locale(provider.language)
            .let { it.getDisplayLanguage(it) }
            .replaceFirstChar { it.titlecase() }
    }

    private fun displayTvItem(binding: ItemProviderTvBinding) {
        binding.root.apply {
            setOnClickListener {
                UserPreferences.currentProvider = provider.provider
                context.toActivity()?.apply {
                    finish()
                    startActivity(Intent(this, this::class.java))
                }
            }
        }

        Glide.with(context)
            .load(provider.logo.takeIf { it.isNotEmpty() }
                ?: R.drawable.ic_provider_default_logo)
            .into(binding.ivProviderLogo)

        binding.tvProviderName.text = provider.name

        binding.tvProviderLanguage.text = Locale(provider.language)
            .let { it.getDisplayLanguage(it) }
            .replaceFirstChar { it.titlecase() }
    }
}