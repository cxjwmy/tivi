/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.showdetails.episodedetails

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import app.tivi.R
import app.tivi.extensions.observeK
import app.tivi.showdetails.ShowDetailsNavigator
import app.tivi.showdetails.ShowDetailsNavigatorViewModel
import app.tivi.util.DaggerBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_episode_details.*
import javax.inject.Inject

class EpisodeDetailsFragment : DaggerBottomSheetFragment() {
    companion object {
        private const val KEY_EPISODE_ID = "episode_id"

        fun create(id: Long): EpisodeDetailsFragment {
            return EpisodeDetailsFragment().apply {
                arguments = bundleOf(KEY_EPISODE_ID to id)
            }
        }
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: EpisodeDetailsViewModel
    private lateinit var controller: EpisodeDetailsEpoxyController

    private lateinit var showDetailsNavigator: ShowDetailsNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EpisodeDetailsViewModel::class.java)
        showDetailsNavigator = ViewModelProviders.of(requireActivity(), viewModelFactory)
                .get(ShowDetailsNavigatorViewModel::class.java)

        arguments?.let {
            viewModel.episodeId = it.getLong(KEY_EPISODE_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_episode_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller = EpisodeDetailsEpoxyController(requireContext(), object : EpisodeDetailsEpoxyController.Callbacks {
        })

        details_rv.setController(controller)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.data.observeK(this) { it?.let(::update) }
    }

    private fun update(viewState: EpisodeDetailsViewState) {
        controller.setData(viewState)
    }
}