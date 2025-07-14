package com.arcane.securestream.adapters.viewholders

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.arcane.securestream.R
import com.arcane.securestream.adapters.AppAdapter
import com.arcane.securestream.database.AppDatabase
import com.arcane.securestream.databinding.ContentMovieCastMobileBinding
import com.arcane.securestream.databinding.ContentMovieCastTvBinding
import com.arcane.securestream.databinding.ContentMovieMobileBinding
import com.arcane.securestream.databinding.ContentMovieRecommendationsMobileBinding
import com.arcane.securestream.databinding.ContentMovieRecommendationsTvBinding
import com.arcane.securestream.databinding.ContentMovieTvBinding
import com.arcane.securestream.databinding.ItemCategorySwiperMobileBinding
import com.arcane.securestream.databinding.ItemMovieGridMobileBinding
import com.arcane.securestream.databinding.ItemMovieGridTvBinding
import com.arcane.securestream.databinding.ItemMovieMobileBinding
import com.arcane.securestream.databinding.ItemMovieTvBinding
import com.arcane.securestream.fragments.genre.GenreMobileFragment
import com.arcane.securestream.fragments.genre.GenreMobileFragmentDirections
import com.arcane.securestream.fragments.genre.GenreTvFragment
import com.arcane.securestream.fragments.genre.GenreTvFragmentDirections
import com.arcane.securestream.fragments.home.HomeMobileFragment
import com.arcane.securestream.fragments.home.HomeMobileFragmentDirections
import com.arcane.securestream.fragments.home.HomeTvFragment
import com.arcane.securestream.fragments.home.HomeTvFragmentDirections
import com.arcane.securestream.fragments.movie.MovieMobileFragment
import com.arcane.securestream.fragments.movie.MovieMobileFragmentDirections
import com.arcane.securestream.fragments.movie.MovieTvFragment
import com.arcane.securestream.fragments.movie.MovieTvFragmentDirections
import com.arcane.securestream.fragments.movies.MoviesMobileFragment
import com.arcane.securestream.fragments.movies.MoviesMobileFragmentDirections
import com.arcane.securestream.fragments.movies.MoviesTvFragment
import com.arcane.securestream.fragments.movies.MoviesTvFragmentDirections
import com.arcane.securestream.fragments.people.PeopleMobileFragment
import com.arcane.securestream.fragments.people.PeopleMobileFragmentDirections
import com.arcane.securestream.fragments.people.PeopleTvFragment
import com.arcane.securestream.fragments.people.PeopleTvFragmentDirections
import com.arcane.securestream.fragments.search.SearchMobileFragment
import com.arcane.securestream.fragments.search.SearchMobileFragmentDirections
import com.arcane.securestream.fragments.search.SearchTvFragment
import com.arcane.securestream.fragments.search.SearchTvFragmentDirections
import com.arcane.securestream.fragments.tv_show.TvShowMobileFragment
import com.arcane.securestream.fragments.tv_show.TvShowMobileFragmentDirections
import com.arcane.securestream.fragments.tv_show.TvShowTvFragment
import com.arcane.securestream.fragments.tv_show.TvShowTvFragmentDirections
import com.arcane.securestream.models.Movie
import com.arcane.securestream.models.TvShow
import com.arcane.securestream.models.Video
import com.arcane.securestream.ui.ShowOptionsMobileDialog
import com.arcane.securestream.ui.ShowOptionsTvDialog
import com.arcane.securestream.ui.SpacingItemDecoration
import com.arcane.securestream.utils.dp
import com.arcane.securestream.utils.format
import com.arcane.securestream.utils.getCurrentFragment
import com.arcane.securestream.utils.toActivity
import java.util.Locale

class MovieViewHolder(
    private val _binding: ViewBinding
) : RecyclerView.ViewHolder(
    _binding.root
) {

    private val context = itemView.context
    private val database = AppDatabase.getInstance(context)
    private lateinit var movie: Movie

    val childRecyclerView: RecyclerView?
        get() = when (_binding) {
            is ContentMovieCastMobileBinding -> _binding.rvMovieCast
            is ContentMovieCastTvBinding -> _binding.hgvMovieCast
            is ContentMovieRecommendationsMobileBinding -> _binding.rvMovieRecommendations
            is ContentMovieRecommendationsTvBinding -> _binding.hgvMovieRecommendations
            else -> null
        }

    fun bind(movie: Movie) {
        this.movie = movie

        when (_binding) {
            is ItemMovieMobileBinding -> displayMobileItem(_binding)
            is ItemMovieTvBinding -> displayTvItem(_binding)
            is ItemMovieGridMobileBinding -> displayGridMobileItem(_binding)
            is ItemMovieGridTvBinding -> displayGridTvItem(_binding)
            is ItemCategorySwiperMobileBinding -> displaySwiperMobileItem(_binding)

            is ContentMovieMobileBinding -> displayMovieMobile(_binding)
            is ContentMovieTvBinding -> displayMovieTv(_binding)
            is ContentMovieCastMobileBinding -> displayCastMobile(_binding)
            is ContentMovieCastTvBinding -> displayCastTv(_binding)
            is ContentMovieRecommendationsMobileBinding -> displayRecommendationsMobile(_binding)
            is ContentMovieRecommendationsTvBinding -> displayRecommendationsTv(_binding)
        }
    }


    private fun displayMobileItem(binding: ItemMovieMobileBinding) {
        binding.root.apply {
            setOnClickListener {
                when (context.toActivity()?.getCurrentFragment()) {
                    is HomeMobileFragment -> {
                        findNavController().navigate(
                            HomeMobileFragmentDirections.actionHomeToMovie(
                                id = movie.id
                            )
                        )
                        if (movie.itemType == AppAdapter.Type.MOVIE_CONTINUE_WATCHING_MOBILE_ITEM) {
                            findNavController().navigate(
                                MovieMobileFragmentDirections.actionMovieToPlayer(
                                    id = movie.id,
                                    title = movie.title,
                                    subtitle = movie.released?.format("yyyy") ?: "",
                                    videoType = Video.Type.Movie(
                                        id = movie.id,
                                        title = movie.title,
                                        releaseDate = movie.released?.format("yyyy-MM-dd") ?: "",
                                        poster = movie.poster ?: "",
                                    ),
                                )
                            )
                        }
                    }
                    is MovieMobileFragment -> findNavController().navigate(
                        MovieMobileFragmentDirections.actionMovieToMovie(
                            id = movie.id
                        )
                    )
                    is TvShowMobileFragment -> findNavController().navigate(
                        TvShowMobileFragmentDirections.actionTvShowToMovie(
                            id = movie.id
                        )
                    )
                }
            }
            setOnLongClickListener {
                ShowOptionsMobileDialog(context, movie)
                    .show()
                true
            }
        }

        Glide.with(context)
            .load(movie.poster)
            .centerCrop()
            .into(binding.ivMoviePoster)

        binding.tvMovieQuality.apply {
            text = movie.quality ?: ""
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieReleasedYear.text = movie.released?.format("yyyy")
            ?: context.getString(R.string.movie_item_type)

        binding.pbMovieProgress.apply {
            val watchHistory = movie.watchHistory

            progress = when {
                watchHistory != null -> (watchHistory.lastPlaybackPositionMillis * 100 / watchHistory.durationMillis.toDouble()).toInt()
                else -> 0
            }
            visibility = when {
                watchHistory != null -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.tvMovieTitle.text = movie.title
    }

    private fun displayTvItem(binding: ItemMovieTvBinding) {
        binding.root.apply {
            setOnClickListener {
                when (context.toActivity()?.getCurrentFragment()) {
                    is HomeTvFragment -> {
                        findNavController().navigate(
                            HomeTvFragmentDirections.actionHomeToMovie(
                                id = movie.id
                            )
                        )
                        if (movie.itemType == AppAdapter.Type.MOVIE_CONTINUE_WATCHING_TV_ITEM) {
                            findNavController().navigate(
                                MovieTvFragmentDirections.actionMovieToPlayer(
                                    id = movie.id,
                                    title = movie.title,
                                    subtitle = movie.released?.format("yyyy") ?: "",
                                    videoType = Video.Type.Movie(
                                        id = movie.id,
                                        title = movie.title,
                                        releaseDate = movie.released?.format("yyyy-MM-dd") ?: "",
                                        poster = movie.poster ?: "",
                                    ),
                                )
                            )
                        }
                    }
                    is MovieTvFragment -> findNavController().navigate(
                        MovieTvFragmentDirections.actionMovieToMovie(
                            id = movie.id
                        )
                    )
                    is TvShowTvFragment -> findNavController().navigate(
                        TvShowTvFragmentDirections.actionTvShowToMovie(
                            id = movie.id
                        )
                    )
                }
            }
            setOnLongClickListener {
                ShowOptionsTvDialog(context, movie)
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
                        is HomeTvFragment -> fragment.updateBackground(movie.banner)
                    }
                }
            }
        }

        Glide.with(context)
            .load(movie.poster)
            .fallback(R.drawable.glide_fallback_cover)
            .centerCrop()
            .into(binding.ivMoviePoster)

        binding.pbMovieProgress.apply {
            val watchHistory = movie.watchHistory

            progress = when {
                watchHistory != null -> (watchHistory.lastPlaybackPositionMillis * 100 / watchHistory.durationMillis.toDouble()).toInt()
                else -> 0
            }
            visibility = when {
                watchHistory != null -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.tvMovieQuality.apply {
            text = movie.quality ?: ""
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieReleasedYear.text = movie.released?.format("yyyy")
            ?: context.getString(R.string.movie_item_type)

        binding.tvMovieTitle.text = movie.title
    }

    private fun displayGridMobileItem(binding: ItemMovieGridMobileBinding) {
        binding.root.apply {
            setOnClickListener {
                when (context.toActivity()?.getCurrentFragment()) {
                    is GenreMobileFragment -> findNavController().navigate(
                        GenreMobileFragmentDirections.actionGenreToMovie(
                            id = movie.id
                        )
                    )
                    is MoviesMobileFragment -> findNavController().navigate(
                        MoviesMobileFragmentDirections.actionMoviesToMovie(
                            id = movie.id
                        )
                    )
                    is PeopleMobileFragment -> findNavController().navigate(
                        PeopleMobileFragmentDirections.actionPeopleToMovie(
                            id = movie.id
                        )
                    )
                    is SearchMobileFragment -> findNavController().navigate(
                        SearchMobileFragmentDirections.actionSearchToMovie(
                            id = movie.id
                        )
                    )
                }
            }
            setOnLongClickListener {
                ShowOptionsMobileDialog(context, movie)
                    .show()
                true
            }
        }

        Glide.with(context)
            .load(movie.poster)
            .centerCrop()
            .into(binding.ivMoviePoster)

        binding.tvMovieQuality.apply {
            text = movie.quality ?: ""
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieReleasedYear.text = movie.released?.format("yyyy")
            ?: context.getString(R.string.movie_item_type)

        binding.pbMovieProgress.apply {
            val watchHistory = movie.watchHistory

            progress = when {
                watchHistory != null -> (watchHistory.lastPlaybackPositionMillis * 100 / watchHistory.durationMillis.toDouble()).toInt()
                else -> 0
            }
            visibility = when {
                watchHistory != null -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.tvMovieTitle.text = movie.title
    }

    private fun displayGridTvItem(binding: ItemMovieGridTvBinding) {
        binding.root.apply {
            setOnClickListener {
                when (context.toActivity()?.getCurrentFragment()) {
                    is GenreTvFragment -> findNavController().navigate(
                        GenreTvFragmentDirections.actionGenreToMovie(
                            id = movie.id
                        )
                    )
                    is MoviesTvFragment -> findNavController().navigate(
                        MoviesTvFragmentDirections.actionMoviesToMovie(
                            id = movie.id
                        )
                    )
                    is PeopleTvFragment -> findNavController().navigate(
                        PeopleTvFragmentDirections.actionPeopleToMovie(
                            id = movie.id
                        )
                    )
                    is SearchTvFragment -> findNavController().navigate(
                        SearchTvFragmentDirections.actionSearchToMovie(
                            id = movie.id
                        )
                    )
                }
            }
            setOnLongClickListener {
                ShowOptionsTvDialog(context, movie)
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

        Glide.with(context)
            .load(movie.poster)
            .fallback(R.drawable.glide_fallback_cover)
            .centerCrop()
            .into(binding.ivMoviePoster)

        binding.pbMovieProgress.apply {
            val watchHistory = movie.watchHistory

            progress = when {
                watchHistory != null -> (watchHistory.lastPlaybackPositionMillis * 100 / watchHistory.durationMillis.toDouble()).toInt()
                else -> 0
            }
            visibility = when {
                watchHistory != null -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.tvMovieQuality.apply {
            text = movie.quality ?: ""
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieReleasedYear.text = movie.released?.format("yyyy")
            ?: context.getString(R.string.movie_item_type)

        binding.tvMovieTitle.text = movie.title
    }

    private fun displaySwiperMobileItem(binding: ItemCategorySwiperMobileBinding) {
        Glide.with(context)
            .load(movie.banner)
            .centerCrop()
            .into(binding.ivSwiperBackground)

        binding.tvSwiperTitle.text = movie.title

        binding.tvSwiperTvShowLastEpisode.text = context.getString(R.string.movie_item_type)

        binding.tvSwiperQuality.apply {
            text = movie.quality
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvSwiperReleased.apply {
            text = movie.released?.format("yyyy")
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvSwiperRating.apply {
            text = movie.rating?.let { String.format(Locale.ROOT, "%.1f", it) } ?: "N/A"
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.ivSwiperRatingIcon.visibility = binding.tvSwiperRating.visibility

        binding.tvSwiperOverview.apply {
            setOnClickListener {
                maxLines = when (maxLines) {
                    2 -> Int.MAX_VALUE
                    else -> 2
                }
            }

            text = movie.overview
        }

        binding.btnSwiperWatchNow.apply {
            setOnClickListener {
                findNavController().navigate(
                    HomeMobileFragmentDirections.actionHomeToMovie(
                        id = movie.id,
                    )
                )
            }
        }

        binding.pbSwiperProgress.apply {
            val watchHistory = movie.watchHistory

            progress = when {
                watchHistory != null -> (watchHistory.lastPlaybackPositionMillis * 100 / watchHistory.durationMillis.toDouble()).toInt()
                else -> 0
            }
            visibility = when {
                watchHistory != null -> View.VISIBLE
                else -> View.GONE
            }
        }
    }


    private fun displayMovieMobile(binding: ContentMovieMobileBinding) {
        binding.ivMoviePoster.run {
            Glide.with(context)
                .load(movie.poster)
                .into(this)
            visibility = when {
                movie.poster.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieTitle.text = movie.title

        binding.tvMovieRating.text = movie.rating?.let { String.format(Locale.ROOT, "%.1f", it) } ?: "N/A"

        binding.tvMovieQuality.apply {
            text = movie.quality
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieReleased.apply {
            text = movie.released?.format("yyyy")
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieRuntime.apply {
            text = movie.runtime?.let {
                val hours = it / 60
                val minutes = it % 60
                when {
                    hours > 0 -> context.getString(
                        R.string.movie_runtime_hours_minutes,
                        hours,
                        minutes
                    )
                    else -> context.getString(R.string.movie_runtime_minutes, minutes)
                }
            }
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieGenres.apply {
            text = movie.genres.joinToString(", ") { it.name }
            visibility = when {
                movie.genres.isEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieOverview.text = movie.overview

        binding.btnMovieWatchNow.apply {
            setOnClickListener {
                findNavController().navigate(
                    MovieMobileFragmentDirections.actionMovieToPlayer(
                        id = movie.id,
                        title = movie.title,
                        subtitle = movie.released?.format("yyyy") ?: "",
                        videoType = Video.Type.Movie(
                            id = movie.id,
                            title = movie.title,
                            releaseDate = movie.released?.format("yyyy-MM-dd") ?: "",
                            poster = movie.poster ?: movie.banner ?: "",
                        ),
                    )
                )
            }
        }

        binding.pbMovieProgress.apply {
            val watchHistory = movie.watchHistory

            progress = when {
                watchHistory != null -> (watchHistory.lastPlaybackPositionMillis * 100 / watchHistory.durationMillis.toDouble()).toInt()
                else -> 0
            }
            visibility = when {
                watchHistory != null -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.btnMovieTrailer.apply {
            val trailer = movie.trailer

            setOnClickListener {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(trailer)
                    )
                )
            }

            visibility = when {
                trailer != null -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.btnMovieFavorite.apply {
            fun Boolean.drawable() = when (this) {
                true -> R.drawable.ic_favorite_enable
                false -> R.drawable.ic_favorite_disable
            }

            setOnClickListener {
                movie.isFavorite = !movie.isFavorite
                database.movieDao().update(movie)

                setImageDrawable(
                    ContextCompat.getDrawable(context, movie.isFavorite.drawable())
                )
            }

            setImageDrawable(
                ContextCompat.getDrawable(context, movie.isFavorite.drawable())
            )
        }
    }

    private fun displayMovieTv(binding: ContentMovieTvBinding) {
        binding.ivMoviePoster.run {
            Glide.with(context)
                .load(movie.poster)
                .into(this)
            visibility = when {
                movie.poster.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieTitle.text = movie.title

        binding.tvMovieRating.text = movie.rating?.let { String.format(Locale.ROOT, "%.1f", it) } ?: "N/A"

        binding.tvMovieQuality.apply {
            text = movie.quality
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieReleased.apply {
            text = movie.released?.format("yyyy")
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieRuntime.apply {
            text = movie.runtime?.let {
                val hours = it / 60
                val minutes = it % 60
                when {
                    hours > 0 -> context.getString(
                        R.string.movie_runtime_hours_minutes,
                        hours,
                        minutes
                    )
                    else -> context.getString(R.string.movie_runtime_minutes, minutes)
                }
            }
            visibility = when {
                text.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieGenres.apply {
            text = movie.genres.joinToString(", ") { it.name }
            visibility = when {
                movie.genres.isEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.tvMovieOverview.text = movie.overview

        binding.btnMovieWatchNow.apply {
            setOnClickListener {
                findNavController().navigate(
                    MovieTvFragmentDirections.actionMovieToPlayer(
                        id = movie.id,
                        title = movie.title,
                        subtitle = movie.released?.format("yyyy") ?: "",
                        videoType = Video.Type.Movie(
                            id = movie.id,
                            title = movie.title,
                            releaseDate = movie.released?.format("yyyy-MM-dd") ?: "",
                            poster = movie.poster ?: movie.banner ?: "",
                        ),
                    )
                )
            }
        }

        binding.pbMovieProgress.apply {
            val watchHistory = movie.watchHistory

            progress = when {
                watchHistory != null -> (watchHistory.lastPlaybackPositionMillis * 100 / watchHistory.durationMillis.toDouble()).toInt()
                else -> 0
            }
            visibility = when {
                watchHistory != null -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.btnMovieTrailer.apply {
            val trailer = movie.trailer

            setOnClickListener {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(trailer)
                    )
                )
            }

            visibility = when {
                trailer != null -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.btnMovieFavorite.apply {
            fun Boolean.drawable() = when (this) {
                true -> R.drawable.ic_favorite_enable
                false -> R.drawable.ic_favorite_disable
            }

            setOnClickListener {
                movie.isFavorite = !movie.isFavorite
                database.movieDao().update(movie)

                setImageDrawable(
                    ContextCompat.getDrawable(context, movie.isFavorite.drawable())
                )
            }

            setImageDrawable(
                ContextCompat.getDrawable(context, movie.isFavorite.drawable())
            )
        }
    }

    private fun displayCastMobile(binding: ContentMovieCastMobileBinding) {
        binding.rvMovieCast.apply {
            adapter = AppAdapter().apply {
                submitList(movie.cast.onEach {
                    it.itemType = AppAdapter.Type.PEOPLE_MOBILE_ITEM
                })
            }
            if (itemDecorationCount == 0) {
                addItemDecoration(SpacingItemDecoration(20.dp(context)))
            }
        }
    }

    private fun displayCastTv(binding: ContentMovieCastTvBinding) {
        binding.hgvMovieCast.apply {
            setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
            adapter = AppAdapter().apply {
                submitList(movie.cast.onEach {
                    it.itemType = AppAdapter.Type.PEOPLE_TV_ITEM
                })
            }
            setItemSpacing(80)
        }
    }

    private fun displayRecommendationsMobile(binding: ContentMovieRecommendationsMobileBinding) {
        binding.rvMovieRecommendations.apply {
            adapter = AppAdapter().apply {
                submitList(movie.recommendations.onEach {
                    when (it) {
                        is Movie -> it.itemType = AppAdapter.Type.MOVIE_MOBILE_ITEM
                        is TvShow -> it.itemType = AppAdapter.Type.TV_SHOW_MOBILE_ITEM
                    }
                })
            }
            if (itemDecorationCount == 0) {
                addItemDecoration(SpacingItemDecoration(10.dp(context)))
            }
        }
    }

    private fun displayRecommendationsTv(binding: ContentMovieRecommendationsTvBinding) {
        binding.hgvMovieRecommendations.apply {
            setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
            adapter = AppAdapter().apply {
                submitList(movie.recommendations.onEach {
                    when (it) {
                        is Movie -> it.itemType = AppAdapter.Type.MOVIE_TV_ITEM
                        is TvShow -> it.itemType = AppAdapter.Type.TV_SHOW_TV_ITEM
                    }
                })
            }
            setItemSpacing(20)
        }
    }
}