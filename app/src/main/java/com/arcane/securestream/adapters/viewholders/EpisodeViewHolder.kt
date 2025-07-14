package com.arcane.securestream.adapters.viewholders

import android.view.View
import android.view.animation.AnimationUtils
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.arcane.securestream.R
import com.arcane.securestream.databinding.ItemEpisodeContinueWatchingMobileBinding
import com.arcane.securestream.databinding.ItemEpisodeContinueWatchingTvBinding
import com.arcane.securestream.databinding.ItemEpisodeMobileBinding
import com.arcane.securestream.databinding.ItemEpisodeTvBinding
import com.arcane.securestream.fragments.home.HomeMobileFragmentDirections
import com.arcane.securestream.fragments.home.HomeTvFragment
import com.arcane.securestream.fragments.home.HomeTvFragmentDirections
import com.arcane.securestream.fragments.season.SeasonMobileFragmentDirections
import com.arcane.securestream.fragments.season.SeasonTvFragmentDirections
import com.arcane.securestream.fragments.tv_show.TvShowMobileFragmentDirections
import com.arcane.securestream.fragments.tv_show.TvShowTvFragmentDirections
import com.arcane.securestream.models.Episode
import com.arcane.securestream.models.Video
import com.arcane.securestream.ui.ShowOptionsMobileDialog
import com.arcane.securestream.ui.ShowOptionsTvDialog
import com.arcane.securestream.utils.format
import com.arcane.securestream.utils.getCurrentFragment
import com.arcane.securestream.utils.toActivity

class EpisodeViewHolder(
    private val _binding: ViewBinding
) : RecyclerView.ViewHolder(
    _binding.root
) {

    private val context = itemView.context
    private lateinit var episode: Episode

    fun bind(episode: Episode) {
        this.episode = episode

        when (_binding) {
            is ItemEpisodeMobileBinding -> displayMobileItem(_binding)
            is ItemEpisodeTvBinding -> displayTvItem(_binding)
            is ItemEpisodeContinueWatchingMobileBinding -> displayContinueWatchingMobileItem(_binding)
            is ItemEpisodeContinueWatchingTvBinding -> displayContinueWatchingTvItem(_binding)
        }
    }


    private fun displayMobileItem(binding: ItemEpisodeMobileBinding) {
        binding.root.apply {
            setOnClickListener {
                findNavController().navigate(
                    SeasonMobileFragmentDirections.actionSeasonToPlayer(
                        id = episode.id,
                        title = episode.tvShow?.title ?: "",
                        subtitle = episode.season?.takeIf { it.number != 0 }?.let { season ->
                            context.getString(
                                R.string.player_subtitle_tv_show,
                                season.number,
                                episode.number,
                                episode.title ?: context.getString(
                                    R.string.episode_number,
                                    episode.number
                                )
                            )
                        } ?: context.getString(
                            R.string.player_subtitle_tv_show_episode_only,
                            episode.number,
                            episode.title ?: context.getString(
                                R.string.episode_number,
                                episode.number
                            )
                        ),
                        videoType = Video.Type.Episode(
                            id = episode.id,
                            number = episode.number,
                            title = episode.title,
                            poster = episode.poster,
                            tvShow = Video.Type.Episode.TvShow(
                                id = episode.tvShow?.id ?: "",
                                title = episode.tvShow?.title ?: "",
                                poster = episode.tvShow?.poster,
                                banner = episode.tvShow?.banner,
                            ),
                            season = Video.Type.Episode.Season(
                                number = episode.season?.number ?: 0,
                                title = episode.season?.title,
                            ),
                        ),
                    )
                )
            }
            setOnLongClickListener {
                ShowOptionsMobileDialog(context, episode)
                    .show()
                true
            }
        }

        binding.ivEpisodePoster.apply {
            clipToOutline = true
            Glide.with(context)
                .load(episode.poster)
                .centerCrop()
                .into(this)
        }

        binding.pbEpisodeProgress.apply {
            val watchHistory = episode.watchHistory

            progress = when {
                watchHistory != null -> (watchHistory.lastPlaybackPositionMillis * 100 / watchHistory.durationMillis.toDouble()).toInt()
                episode.isWatched -> 100
                else -> 0
            }
            visibility = when {
                watchHistory != null -> View.VISIBLE
                episode.isWatched -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.tvEpisodeInfo.text = context.getString(
            R.string.episode_number,
            episode.number
        )

        binding.tvEpisodeTitle.text = episode.title ?: context.getString(
            R.string.episode_number,
            episode.number
        )

        binding.tvEpisodeReleased.apply {
            text = episode.released?.let { " • ${it.format("yyyy-MM-dd")}" }
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    private fun displayTvItem(binding: ItemEpisodeTvBinding) {
        binding.root.apply {
            setOnClickListener {
                findNavController().navigate(
                    SeasonTvFragmentDirections.actionSeasonToPlayer(
                        id = episode.id,
                        title = episode.tvShow?.title ?: "",
                        subtitle = episode.season?.takeIf { it.number != 0 }?.let { season ->
                            context.getString(
                                R.string.player_subtitle_tv_show,
                                season.number,
                                episode.number,
                                episode.title ?: context.getString(
                                    R.string.episode_number,
                                    episode.number
                                )
                            )
                        } ?: context.getString(
                            R.string.player_subtitle_tv_show_episode_only,
                            episode.number,
                            episode.title ?: context.getString(
                                R.string.episode_number,
                                episode.number
                            )
                        ),
                        videoType = Video.Type.Episode(
                            id = episode.id,
                            number = episode.number,
                            title = episode.title,
                            poster = episode.poster,
                            tvShow = Video.Type.Episode.TvShow(
                                id = episode.tvShow?.id ?: "",
                                title = episode.tvShow?.title ?: "",
                                poster = episode.tvShow?.poster,
                                banner = episode.tvShow?.banner,
                            ),
                            season = Video.Type.Episode.Season(
                                number = episode.season?.number ?: 0,
                                title = episode.season?.title,
                            ),
                        ),
                    )
                )
            }
            setOnLongClickListener {
                ShowOptionsTvDialog(context, episode)
                    .show()
                true
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

        binding.ivEpisodePoster.apply {
            clipToOutline = true
            Glide.with(context)
                .load(episode.poster)
                .fallback(R.drawable.glide_fallback_cover)
                .centerCrop()
                .into(this)
        }

        binding.pbEpisodeProgress.apply {
            val watchHistory = episode.watchHistory

            progress = when {
                watchHistory != null -> (watchHistory.lastPlaybackPositionMillis * 100 / watchHistory.durationMillis.toDouble()).toInt()
                episode.isWatched -> 100
                else -> 0
            }
            visibility = when {
                watchHistory != null -> View.VISIBLE
                episode.isWatched -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.tvEpisodeInfo.text = context.getString(
            R.string.episode_number,
            episode.number
        )

        binding.tvEpisodeTitle.text = episode.title ?: context.getString(
            R.string.episode_number,
            episode.number
        )

        binding.tvEpisodeReleased.apply {
            text = episode.released?.format("EEEE - MMMM dd, yyyy")
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    private fun displayContinueWatchingMobileItem(binding: ItemEpisodeContinueWatchingMobileBinding) {
        binding.root.apply {
            setOnClickListener {
                findNavController().navigate(
                    HomeMobileFragmentDirections.actionHomeToTvShow(
                        id = episode.tvShow?.id ?: "",
                    )
                )
                findNavController().navigate(
                    TvShowMobileFragmentDirections.actionTvShowToPlayer(
                        id = episode.id,
                        title = episode.tvShow?.title ?: "",
                        subtitle = episode.season?.takeIf { it.number != 0 }?.let { season ->
                            context.getString(
                                R.string.player_subtitle_tv_show,
                                season.number,
                                episode.number,
                                episode.title ?: context.getString(
                                    R.string.episode_number,
                                    episode.number
                                )
                            )
                        } ?: context.getString(
                            R.string.player_subtitle_tv_show_episode_only,
                            episode.number,
                            episode.title ?: context.getString(
                                R.string.episode_number,
                                episode.number
                            )
                        ),
                        videoType = Video.Type.Episode(
                            id = episode.id,
                            number = episode.number,
                            title = episode.title,
                            poster = episode.poster,
                            tvShow = Video.Type.Episode.TvShow(
                                id = episode.tvShow?.id ?: "",
                                title = episode.tvShow?.title ?: "",
                                poster = episode.tvShow?.poster,
                                banner = episode.tvShow?.banner,
                            ),
                            season = Video.Type.Episode.Season(
                                number = episode.season?.number ?: 0,
                                title = episode.season?.title,
                            ),
                        ),
                    )
                )
            }
            setOnLongClickListener {
                ShowOptionsMobileDialog(context, episode)
                    .show()
                true
            }
        }

        binding.ivEpisodeTvShowPoster.apply {
            clipToOutline = true
            Glide.with(context)
                .load(episode.tvShow?.poster ?: episode.tvShow?.banner ?: episode.poster)
                .centerCrop()
                .into(this)
        }

        binding.pbEpisodeProgress.apply {
            val watchHistory = episode.watchHistory

            progress = when {
                watchHistory != null -> (watchHistory.lastPlaybackPositionMillis * 100 / watchHistory.durationMillis.toDouble()).toInt()
                else -> 0
            }
            visibility = when {
                watchHistory != null -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.tvEpisodeTvShowTitle.text = episode.tvShow?.title ?: ""

        binding.tvEpisodeInfo.text = episode.season?.takeIf { it.number != 0 }?.let { season ->
            context.getString(
                R.string.episode_item_info,
                season.number,
                episode.number,
                episode.title ?: context.getString(
                    R.string.episode_number,
                    episode.number
                )
            )
        } ?: context.getString(
            R.string.episode_item_info_episode_only,
            episode.number,
            episode.title ?: context.getString(
                R.string.episode_number,
                episode.number
            )
        )
    }

    private fun displayContinueWatchingTvItem(binding: ItemEpisodeContinueWatchingTvBinding) {
        binding.root.apply {
            setOnClickListener {
                findNavController().navigate(
                    HomeTvFragmentDirections.actionHomeToTvShow(
                        id = episode.tvShow?.id ?: "",
                    )
                )
                findNavController().navigate(
                    TvShowTvFragmentDirections.actionTvShowToPlayer(
                        id = episode.id,
                        title = episode.tvShow?.title ?: "",
                        subtitle = episode.season?.takeIf { it.number != 0 }?.let { season ->
                            context.getString(
                                R.string.player_subtitle_tv_show,
                                season.number,
                                episode.number,
                                episode.title ?: context.getString(
                                    R.string.episode_number,
                                    episode.number
                                )
                            )
                        } ?: context.getString(
                            R.string.player_subtitle_tv_show_episode_only,
                            episode.number,
                            episode.title ?: context.getString(
                                R.string.episode_number,
                                episode.number
                            )
                        ),
                        videoType = Video.Type.Episode(
                            id = episode.id,
                            number = episode.number,
                            title = episode.title,
                            poster = episode.poster,
                            tvShow = Video.Type.Episode.TvShow(
                                id = episode.tvShow?.id ?: "",
                                title = episode.tvShow?.title ?: "",
                                poster = episode.tvShow?.poster,
                                banner = episode.tvShow?.banner,
                            ),
                            season = Video.Type.Episode.Season(
                                number = episode.season?.number ?: 0,
                                title = episode.season?.title,
                            ),
                        ),
                    )
                )
            }
            setOnLongClickListener {
                ShowOptionsTvDialog(context, episode)
                    .show()
                true
            }
            setOnFocusChangeListener { _, hasFocus ->
                val animation = when {
                    hasFocus -> AnimationUtils.loadAnimation(context, R.anim.zoom_in)
                    else -> AnimationUtils.loadAnimation(context, R.anim.zoom_out)
                }
                binding.root.startAnimation(animation)
                animation.fillAfter = true

                if (hasFocus) {
                    when (val fragment = context.toActivity()?.getCurrentFragment()) {
                        is HomeTvFragment -> fragment.updateBackground(episode.tvShow?.banner)
                    }
                }
            }
        }

        binding.ivEpisodeTvShowPoster.apply {
            clipToOutline = true
            Glide.with(context)
                .load(episode.tvShow?.poster ?: episode.tvShow?.banner ?: episode.poster)
                .fallback(R.drawable.glide_fallback_cover)
                .centerCrop()
                .into(this)
        }

        binding.pbEpisodeProgress.apply {
            val watchHistory = episode.watchHistory

            progress = when {
                watchHistory != null -> (watchHistory.lastPlaybackPositionMillis * 100 / watchHistory.durationMillis.toDouble()).toInt()
                episode.isWatched -> 100
                else -> 0
            }
            visibility = when {
                watchHistory != null -> View.VISIBLE
                episode.isWatched -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.tvEpisodeTvShowTitle.text = episode.tvShow?.title ?: ""

        binding.tvEpisodeInfo.text = episode.season?.takeIf { it.number != 0 }?.let { season ->
            context.getString(
                R.string.episode_item_info,
                season.number,
                episode.number,
                episode.title ?: context.getString(
                    R.string.episode_number,
                    episode.number
                )
            )
        } ?: context.getString(
            R.string.episode_item_info_episode_only,
            episode.number,
            episode.title ?: context.getString(
                R.string.episode_number,
                episode.number
            )
        )
    }
}