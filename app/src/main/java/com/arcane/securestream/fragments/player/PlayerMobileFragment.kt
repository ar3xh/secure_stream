package com.arcane.securestream.fragments.player

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.SubtitleView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arcane.securestream.R
import com.arcane.securestream.database.AppDatabase
import com.arcane.securestream.databinding.ContentExoControllerMobileBinding
import com.arcane.securestream.databinding.FragmentPlayerMobileBinding
import com.arcane.securestream.models.Episode
import com.arcane.securestream.models.Movie
import com.arcane.securestream.models.Video
import com.arcane.securestream.models.WatchItem
import com.arcane.securestream.utils.MediaServer
import com.arcane.securestream.utils.UserPreferences
import com.arcane.securestream.utils.getFileName
import com.arcane.securestream.utils.next
import com.arcane.securestream.utils.plus
import com.arcane.securestream.utils.setMediaServerId
import com.arcane.securestream.utils.setMediaServers
import com.arcane.securestream.utils.toSubtitleMimeType
import com.arcane.securestream.utils.viewModelsFactory
import kotlinx.coroutines.launch
import okhttp3.internal.userAgent
import java.util.Calendar
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class PlayerMobileFragment : Fragment() {

    private var _binding: FragmentPlayerMobileBinding? = null
    private val binding get() = _binding!!

    private val PlayerControlView.binding
        get() = ContentExoControllerMobileBinding.bind(this.findViewById(R.id.cl_exo_controller))

    private val args by navArgs<PlayerMobileFragmentArgs>()
    private val database by lazy { AppDatabase.getInstance(requireContext()) }
    private val viewModel by viewModelsFactory { PlayerViewModel(args.videoType, args.id) }

    private lateinit var player: ExoPlayer
    private lateinit var httpDataSource: HttpDataSource.Factory
    private lateinit var dataSourceFactory: DataSource.Factory
    private lateinit var mediaSession: MediaSession

    private var servers = listOf<Video.Server>()

    private val pickLocalSubtitle = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        requireContext().contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        val fileName = uri.getFileName(requireContext()) ?: uri.toString()

        val currentPosition = player.currentPosition
        val currentSubtitleConfigurations = player.currentMediaItem?.localConfiguration?.subtitleConfigurations?.map {
            MediaItem.SubtitleConfiguration.Builder(it.uri)
                .setMimeType(it.mimeType)
                .setLabel(it.label)
                .setLanguage(it.language)
                .setSelectionFlags(0)
                .build()
        } ?: listOf()
        player.setMediaItem(
            MediaItem.Builder()
                .setUri(player.currentMediaItem?.localConfiguration?.uri)
                .setMimeType(player.currentMediaItem?.localConfiguration?.mimeType)
                .setSubtitleConfigurations(currentSubtitleConfigurations
                        + MediaItem.SubtitleConfiguration.Builder(uri)
                    .setMimeType(fileName.toSubtitleMimeType())
                    .setLabel(fileName)
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build()
                )
                .setMediaMetadata(player.mediaMetadata)
                .build()
        )
        player.seekTo(currentPosition)
        player.play()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerMobileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        initializeVideo()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).collect { state ->
                when (state) {
                    PlayerViewModel.State.LoadingServers -> {}
                    is PlayerViewModel.State.SuccessLoadingServers -> {
                        servers = state.servers
                        player.playlistMetadata = MediaMetadata.Builder()
                            .setTitle(state.toString())
                            .setMediaServers(state.servers.map {
                                MediaServer(
                                    id = it.id,
                                    name = it.name,
                                )
                            })
                            .build()
                        binding.settings.setOnServerSelectedListener { server ->
                            viewModel.getVideo(state.servers.find { server.id == it.id }!!)
                        }
                        viewModel.getVideo(state.servers.first())
                    }

                    is PlayerViewModel.State.FailedLoadingServers -> {
                        Toast.makeText(
                            requireContext(),
                            state.error.message ?: "",
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().navigateUp()
                    }

                    is PlayerViewModel.State.LoadingVideo -> {
                        player.setMediaItem(
                            MediaItem.Builder()
                                .setUri(Uri.parse(""))
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setMediaServerId(state.server.id)
                                        .build()
                                )
                                .build()
                        )
                    }

                    is PlayerViewModel.State.SuccessLoadingVideo -> {
                        displayVideo(state.video, state.server)
                    }

                    is PlayerViewModel.State.FailedLoadingVideo -> {
                        Toast.makeText(
                            requireContext(),
                            "${state.server.name}: ${state.error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        servers.getOrNull(servers.indexOf(state.server) + 1)?.let {
                            viewModel.getVideo(it)
                        }
                    }

                    PlayerViewModel.State.LoadingSubtitles -> {}
                    is PlayerViewModel.State.SuccessLoadingSubtitles -> {
                        binding.settings.openSubtitles = state.subtitles
                    }
                    is PlayerViewModel.State.FailedLoadingSubtitles -> {}

                    PlayerViewModel.State.DownloadingOpenSubtitle -> {}
                    is PlayerViewModel.State.SuccessDownloadingOpenSubtitle -> {
                        val fileName = state.uri.getFileName(requireContext()) ?: state.uri.toString()

                        val currentPosition = player.currentPosition
                        val currentSubtitleConfigurations = player.currentMediaItem?.localConfiguration?.subtitleConfigurations?.map {
                            MediaItem.SubtitleConfiguration.Builder(it.uri)
                                .setMimeType(it.mimeType)
                                .setLabel(it.label)
                                .setLanguage(it.language)
                                .setSelectionFlags(0)
                                .build()
                        } ?: listOf()
                        player.setMediaItem(
                            MediaItem.Builder()
                                .setUri(player.currentMediaItem?.localConfiguration?.uri)
                                .setMimeType(player.currentMediaItem?.localConfiguration?.mimeType)
                                .setSubtitleConfigurations(currentSubtitleConfigurations
                                        + MediaItem.SubtitleConfiguration.Builder(state.uri)
                                    .setMimeType(fileName.toSubtitleMimeType())
                                    .setLabel(fileName)
                                    .setLanguage(state.subtitle.languageName)
                                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                                    .build()
                                )
                                .setMediaMetadata(player.mediaMetadata)
                                .build()
                        )
                        UserPreferences.subtitleName = (state.subtitle.languageName ?: fileName).substringBefore(" ")
                        player.seekTo(currentPosition)
                        player.play()
                    }
                    is PlayerViewModel.State.FailedDownloadingOpenSubtitle -> {
                        Toast.makeText(
                            requireContext(),
                            "${state.subtitle.subFileName}: ${state.error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        binding.pvPlayer.useController = !isInPictureInPictureMode
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
    }

    fun onUserLeaveHint() {
        if (::player.isInitialized && player.isPlaying) {
            enterPIPMode()
        }
    }

    override fun onStop() {
        super.onStop()
        player.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        WindowCompat.getInsetsController(
            requireActivity().window,
            requireActivity().window.decorView
        ).run {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            show(WindowInsetsCompat.Type.systemBars())
        }
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        player.release()
        mediaSession.release()
        _binding = null
    }


    private fun initializeVideo() {
        WindowCompat.getInsetsController(
            requireActivity().window,
            requireActivity().window.decorView
        ).run {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        httpDataSource = DefaultHttpDataSource.Factory()
        dataSourceFactory = DefaultDataSource.Factory(requireContext(), httpDataSource)
        player = ExoPlayer.Builder(requireContext())
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build().also { player ->
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .build(),
                    true,
                )

                mediaSession = MediaSession.Builder(requireContext(), player)
                    .build()
            }

        binding.pvPlayer.player = player
        binding.settings.player = player
        binding.settings.subtitleView = binding.pvPlayer.subtitleView

        binding.pvPlayer.resizeMode = UserPreferences.playerResize.resizeMode
        binding.pvPlayer.subtitleView?.apply {
            setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * UserPreferences.captionTextSize)
            setStyle(UserPreferences.captionStyle)
        }

        binding.pvPlayer.controller.binding.btnExoBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.pvPlayer.controller.binding.tvExoTitle.text = args.title

        binding.pvPlayer.controller.binding.tvExoSubtitle.text = args.subtitle

        binding.pvPlayer.controller.binding.btnExoExternalPlayer.setOnClickListener {
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.player_external_player_error_video),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.pvPlayer.controller.binding.btnExoLock.setOnClickListener {
            binding.pvPlayer.controller.binding.gControlsLock.visibility = View.GONE
            binding.pvPlayer.controller.binding.btnExoUnlock.visibility = View.VISIBLE
        }

        binding.pvPlayer.controller.binding.btnExoUnlock.setOnClickListener {
            binding.pvPlayer.controller.binding.gControlsLock.visibility = View.VISIBLE
            binding.pvPlayer.controller.binding.btnExoUnlock.visibility = View.GONE
        }

        binding.pvPlayer.controller.binding.btnExoPictureInPicture.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.player_picture_in_picture_not_supported),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                enterPIPMode()
            }
        }

        binding.pvPlayer.controller.binding.btnExoAspectRatio.setOnClickListener {
            UserPreferences.playerResize = UserPreferences.playerResize.next()
            binding.pvPlayer.controllerShowTimeoutMs = binding.pvPlayer.controllerShowTimeoutMs

            Toast.makeText(
                requireContext(),
                requireContext().getString(UserPreferences.playerResize.stringRes),
                Toast.LENGTH_SHORT
            ).show()
            binding.pvPlayer.resizeMode = UserPreferences.playerResize.resizeMode
        }

        binding.pvPlayer.controller.binding.exoSettings.setOnClickListener {
            binding.pvPlayer.controllerShowTimeoutMs = binding.pvPlayer.controllerShowTimeoutMs
            binding.settings.show()
        }

        binding.settings.setOnLocalSubtitlesClickedListener {
            pickLocalSubtitle.launch(
                arrayOf(
                    "text/plain",
                    "text/str",
                    "application/octet-stream",
                    MimeTypes.TEXT_UNKNOWN,
                    MimeTypes.TEXT_VTT,
                    MimeTypes.TEXT_SSA,
                    MimeTypes.APPLICATION_TTML,
                    MimeTypes.APPLICATION_MP4VTT,
                    MimeTypes.APPLICATION_SUBRIP,
                )
            )
        }

        binding.settings.setOnOpenSubtitleSelectedListener { subtitle ->
            viewModel.downloadSubtitle(subtitle.openSubtitle)
        }
    }

    private fun displayVideo(video: Video, server: Video.Server) {
        val currentPosition = player.currentPosition

        httpDataSource.setDefaultRequestProperties(
            mapOf(
                "User-Agent" to userAgent,
            ) + (video.headers ?: emptyMap())
        )

        player.setMediaItem(
            MediaItem.Builder()
                .setUri(Uri.parse(video.source))
                .setMimeType(video.type)
                .setSubtitleConfigurations(video.subtitles.map { subtitle ->
                    MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitle.file))
                        .setMimeType(subtitle.file.toSubtitleMimeType())
                        .setLabel(subtitle.label)
                        .setSelectionFlags(if (subtitle.default) C.SELECTION_FLAG_DEFAULT else 0)
                        .build()
                })
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setMediaServerId(server.id)
                        .build()
                )
                .build()
        )

        binding.pvPlayer.controller.binding.btnExoExternalPlayer.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(video.source), "video/*")

                putExtra("title", when (val videoType = args.videoType as Video.Type) {
                    is Video.Type.Movie -> videoType.title
                    is Video.Type.Episode -> "${videoType.tvShow.title} • S${videoType.season.number} E${videoType.number}"
                })
                putExtra("position", currentPosition)
            }
            startActivity(
                Intent.createChooser(
                    intent,
                    requireContext().getString(R.string.player_external_player_title)
                )
            )
        }

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                binding.pvPlayer.keepScreenOn = isPlaying

                val hasUri = player.currentMediaItem?.localConfiguration?.uri
                    ?.toString()?.isNotEmpty()
                    ?: false

                if (!isPlaying && hasUri) {
                    val watchItem = when (val videoType = args.videoType as Video.Type) {
                        is Video.Type.Movie -> database.movieDao().getById(videoType.id)
                        is Video.Type.Episode -> database.episodeDao().getById(videoType.id)
                    }

                    when {
                        player.hasStarted() && !player.hasFinished() -> {
                            watchItem?.isWatched = false
                            watchItem?.watchedDate = null
                            watchItem?.watchHistory = WatchItem.WatchHistory(
                                lastEngagementTimeUtcMillis = System.currentTimeMillis(),
                                lastPlaybackPositionMillis = player.currentPosition,
                                durationMillis = player.duration,
                            )
                        }

                        player.hasFinished() -> {
                            watchItem?.isWatched = true
                            watchItem?.watchedDate = Calendar.getInstance()
                            watchItem?.watchHistory = null
                        }
                    }

                    when (val videoType = args.videoType as Video.Type) {
                        is Video.Type.Movie -> {
                            val movie = watchItem as Movie
                            database.movieDao().update(movie)
                        }

                        is Video.Type.Episode -> {
                            val episode = watchItem as Episode
                            if (player.hasFinished()) {
                                database.episodeDao().resetProgressionFromEpisode(videoType.id)
                            }
                            database.episodeDao().update(episode)

                            episode.tvShow?.let { tvShow ->
                                database.tvShowDao().getById(tvShow.id)
                            }?.let { tvShow ->
                                database.tvShowDao().save(tvShow.copy().apply {
                                    merge(tvShow)
                                    isWatching = true
                                })
                            }
                        }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.e("PlayerMobileFragment", "onPlayerError: ", error)
            }
        })

        if (currentPosition == 0L) {
            val watchItem = when (val videoType = args.videoType as Video.Type) {
                is Video.Type.Movie -> database.movieDao().getById(videoType.id)
                is Video.Type.Episode -> database.episodeDao().getById(videoType.id)
            }
            val lastPlaybackPositionMillis = watchItem?.watchHistory
                ?.let { it.lastPlaybackPositionMillis - 10.seconds.inWholeMilliseconds }

            player.seekTo(lastPlaybackPositionMillis ?: 0)
        } else {
            player.seekTo(currentPosition)
        }

        player.prepare()
        player.play()
    }

    private fun enterPIPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.pvPlayer.useController = false
            requireActivity().enterPictureInPictureMode(
                PictureInPictureParams.Builder()
                    .build()
            )
        }
    }


    private fun ExoPlayer.hasStarted(): Boolean {
        return (this.currentPosition > (this.duration * 0.03) || this.currentPosition > 2.minutes.inWholeMilliseconds)
    }

    private fun ExoPlayer.hasFinished(): Boolean {
        return (this.currentPosition > (this.duration * 0.90))
    }
}