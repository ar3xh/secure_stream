package com.arcane.securestream.providers

import com.arcane.securestream.adapters.AppAdapter
import com.arcane.securestream.models.Category
import com.arcane.securestream.models.Episode
import com.arcane.securestream.models.Genre
import com.arcane.securestream.models.Movie
import com.arcane.securestream.models.People
import com.arcane.securestream.models.TvShow
import com.arcane.securestream.models.Video

interface Provider {

    val name: String
    val logo: String
    val language: String

    suspend fun getHome(): List<Category>

    suspend fun search(query: String, page: Int = 1): List<AppAdapter.Item>

    suspend fun getMovies(page: Int = 1): List<Movie>

    suspend fun getTvShows(page: Int = 1): List<TvShow>

    suspend fun getMovie(id: String): Movie

    suspend fun getTvShow(id: String): TvShow

    suspend fun getEpisodesBySeason(seasonId: String): List<Episode>

    suspend fun getGenre(id: String, page: Int = 1): Genre

    suspend fun getPeople(id: String, page: Int = 1): People

    suspend fun getServers(id: String, videoType: Video.Type): List<Video.Server>

    suspend fun getVideo(server: Video.Server): Video

    companion object {
        val providers = listOf(
            SflixProvider,
            AnyMovieProvider,
            HiAnimeProvider,
            SerienStreamProvider,
            TmdbProvider,
            SuperStreamProvider,
            StreamingCommunityProvider,
            AnimeWorldProvider,
            AniWorldProvider,
            RidomoviesProvider,
            OtakufrProvider,
            WiflixProvider,
            MStreamProvider,
            FrenchAnimeProvider,
        )
    }
}