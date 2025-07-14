package com.arcane.securestream.adapters.viewholders

import android.view.animation.AnimationUtils
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.arcane.securestream.R
import com.arcane.securestream.databinding.ItemPeopleMobileBinding
import com.arcane.securestream.databinding.ItemPeopleTvBinding
import com.arcane.securestream.fragments.movie.MovieMobileFragment
import com.arcane.securestream.fragments.movie.MovieMobileFragmentDirections
import com.arcane.securestream.fragments.movie.MovieTvFragment
import com.arcane.securestream.fragments.movie.MovieTvFragmentDirections
import com.arcane.securestream.fragments.tv_show.TvShowMobileFragment
import com.arcane.securestream.fragments.tv_show.TvShowMobileFragmentDirections
import com.arcane.securestream.fragments.tv_show.TvShowTvFragment
import com.arcane.securestream.fragments.tv_show.TvShowTvFragmentDirections
import com.arcane.securestream.models.People
import com.arcane.securestream.utils.getCurrentFragment
import com.arcane.securestream.utils.toActivity

class PeopleViewHolder(
    private val _binding: ViewBinding
) : RecyclerView.ViewHolder(
    _binding.root
) {

    private val context = itemView.context
    private lateinit var people: People

    fun bind(people: People) {
        this.people = people

        when (_binding) {
            is ItemPeopleMobileBinding -> displayMobileItem(_binding)
            is ItemPeopleTvBinding -> displayTvItem(_binding)
        }
    }


    private fun displayMobileItem(binding: ItemPeopleMobileBinding) {
        binding.root.apply {
            setOnClickListener {
                when (context.toActivity()?.getCurrentFragment()) {
                    is MovieMobileFragment -> findNavController().navigate(
                        MovieMobileFragmentDirections.actionMovieToPeople(
                            id = people.id,
                            name = people.name,
                            image = people.image,
                        )
                    )
                    is TvShowMobileFragment -> findNavController().navigate(
                        TvShowMobileFragmentDirections.actionTvShowToPeople(
                            id = people.id,
                            name = people.name,
                            image = people.image,
                        )
                    )
                }
            }
        }

        binding.ivPeopleImage.apply {
            clipToOutline = true
            Glide.with(context)
                .load(people.image)
                .placeholder(R.drawable.ic_person_placeholder)
                .centerCrop()
                .into(this)
        }

        binding.tvPeopleName.text = people.name
    }

    private fun displayTvItem(binding: ItemPeopleTvBinding) {
        binding.root.apply {
            setOnClickListener {
                when (context.toActivity()?.getCurrentFragment()) {
                    is MovieTvFragment -> findNavController().navigate(
                        MovieTvFragmentDirections.actionMovieToPeople(
                            id = people.id,
                            name = people.name,
                            image = people.image,
                        )
                    )
                    is TvShowTvFragment -> findNavController().navigate(
                        TvShowTvFragmentDirections.actionTvShowToPeople(
                            id = people.id,
                            name = people.name,
                            image = people.image,
                        )
                    )
                }
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

        binding.ivPeopleImage.apply {
            clipToOutline = true
            Glide.with(context)
                .load(people.image)
                .placeholder(R.drawable.ic_person_placeholder)
                .centerCrop()
                .into(this)
        }

        binding.tvPeopleName.text = people.name
    }
}