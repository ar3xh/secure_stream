package com.arcane.securestream.adapters.viewholders

import android.view.animation.AnimationUtils
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.arcane.securestream.R
import com.arcane.securestream.databinding.ItemSeasonMobileBinding
import com.arcane.securestream.databinding.ItemSeasonTvBinding
import com.arcane.securestream.fragments.tv_show.TvShowMobileFragmentDirections
import com.arcane.securestream.fragments.tv_show.TvShowTvFragmentDirections
import com.arcane.securestream.models.Season

class SeasonViewHolder(
    private val _binding: ViewBinding
) : RecyclerView.ViewHolder(
    _binding.root
) {

    private val context = itemView.context
    private lateinit var season: Season

    fun bind(season: Season) {
        this.season = season

        when (_binding) {
            is ItemSeasonMobileBinding -> displayMobileItem(_binding)
            is ItemSeasonTvBinding -> displayTvItem(_binding)
        }
    }


    private fun displayMobileItem(binding: ItemSeasonMobileBinding) {
        binding.root.apply {
            setOnClickListener {
                findNavController().navigate(
                    TvShowMobileFragmentDirections.actionTvShowToSeason(
                        tvShowId = season.tvShow?.id ?: "",
                        tvShowTitle = season.tvShow?.title ?: "",
                        tvShowPoster = season.tvShow?.poster,
                        tvShowBanner = season.tvShow?.banner,
                        seasonId = season.id,
                        seasonNumber = season.number,
                        seasonTitle = season.title,
                    )
                )
            }
        }

        binding.ivSeasonPoster.apply {
            clipToOutline = true
            Glide.with(context)
                .load(season.poster)
                .fallback(R.drawable.glide_fallback_cover)
                .centerCrop()
                .into(this)
        }

        binding.tvSeasonTitle.text = season.title ?: context.getString(
            R.string.season_number,
            season.number
        )
    }

    private fun displayTvItem(binding: ItemSeasonTvBinding) {
        binding.root.apply {
            setOnClickListener {
                findNavController().navigate(
                    TvShowTvFragmentDirections.actionTvShowToSeason(
                        tvShowId = season.tvShow?.id ?: "",
                        tvShowTitle = season.tvShow?.title ?: "",
                        tvShowPoster = season.tvShow?.poster,
                        tvShowBanner = season.tvShow?.banner,
                        seasonId = season.id,
                        seasonNumber = season.number,
                        seasonTitle = season.title,
                    )
                )
            }
            setOnFocusChangeListener { _, hasFocus ->
                val animation = when {
                    hasFocus -> AnimationUtils.loadAnimation(context, R.anim.zoom_in)
                    else -> AnimationUtils.loadAnimation(context, R.anim.zoom_out)
                }
                binding.root.startAnimation(animation)
                animation.fillAfter = true
            }
        }

        binding.ivSeasonPoster.apply {
            clipToOutline = true
            Glide.with(context)
                .load(season.poster)
                .fallback(R.drawable.glide_fallback_cover)
                .centerCrop()
                .into(this)
        }

        binding.tvSeasonTitle.text = season.title ?: context.getString(
            R.string.season_number,
            season.number
        )
    }
}