package com.arcane.securestream.fragments.genre

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.arcane.securestream.R
import com.arcane.securestream.adapters.AppAdapter
import com.arcane.securestream.database.AppDatabase
import com.arcane.securestream.databinding.FragmentGenreTvBinding
import com.arcane.securestream.models.Genre
import com.arcane.securestream.models.Movie
import com.arcane.securestream.models.TvShow
import com.arcane.securestream.utils.viewModelsFactory
import kotlinx.coroutines.launch

class GenreTvFragment : Fragment() {

    private var _binding: FragmentGenreTvBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<GenreTvFragmentArgs>()
    private val database by lazy { AppDatabase.getInstance(requireContext()) }
    private val viewModel by viewModelsFactory { GenreViewModel(args.id, database) }

    private val appAdapter = AppAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenreTvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeGenre()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { state ->
                when (state) {
                    GenreViewModel.State.Loading -> binding.isLoading.apply {
                        root.visibility = View.VISIBLE
                        pbIsLoading.visibility = View.VISIBLE
                        gIsLoadingRetry.visibility = View.GONE
                    }
                    GenreViewModel.State.LoadingMore -> appAdapter.isLoading = true
                    is GenreViewModel.State.SuccessLoading -> {
                        displayGenre(state.genre, state.hasMore)
                        appAdapter.isLoading = false
                        binding.isLoading.root.visibility = View.GONE
                    }
                    is GenreViewModel.State.FailedLoading -> {
                        Toast.makeText(
                            requireContext(),
                            state.error.message ?: "",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (appAdapter.isLoading) {
                            appAdapter.isLoading = false
                        } else {
                            binding.isLoading.apply {
                                pbIsLoading.visibility = View.GONE
                                gIsLoadingRetry.visibility = View.VISIBLE
                                btnIsLoadingRetry.setOnClickListener {
                                    viewModel.getGenre(args.id)
                                }
                                btnIsLoadingRetry.requestFocus()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun initializeGenre() {
        binding.vgvGenre.apply {
            adapter = appAdapter.apply {
                stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            }
            setItemSpacing(requireContext().resources.getDimension(R.dimen.genre_spacing).toInt())
        }
    }

    private fun displayGenre(genre: Genre, hasMore: Boolean) {
        binding.tvGenreName.text = getString(
            R.string.genre_header_name,
            genre.name.takeIf { it.isNotEmpty() } ?: args.name
        )

        appAdapter.submitList(genre.shows.onEach {
            when (it) {
                is Movie -> it.itemType = AppAdapter.Type.MOVIE_GRID_TV_ITEM
                is TvShow -> it.itemType = AppAdapter.Type.TV_SHOW_GRID_TV_ITEM
            }
        })

        if (hasMore) {
            appAdapter.setOnLoadMoreListener { viewModel.loadMoreGenreShows() }
        } else {
            appAdapter.setOnLoadMoreListener(null)
        }
    }
}