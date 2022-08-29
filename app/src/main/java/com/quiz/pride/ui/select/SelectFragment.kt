package com.quiz.pride.ui.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.quiz.pride.common.startActivity
import com.quiz.pride.databinding.SelectFragmentBinding
import com.quiz.pride.ui.info.InfoActivity
import com.quiz.pride.ui.settings.SettingsActivity
import com.quiz.pride.utils.setSafeOnClickListener
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel

class SelectFragment : Fragment() {
    private lateinit var binding: SelectFragmentBinding
    private val selectViewModel: SelectViewModel by lifecycleScope.viewModel(this)

    companion object {
        fun newInstance() = SelectFragment()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = SelectFragmentBinding.inflate(inflater)
        val root = binding.root

        binding.cardStart.setSafeOnClickListener {
            selectViewModel.navigateToSelectGame()
        }

        binding.cardLearn.setSafeOnClickListener {
            selectViewModel.navigateToInfo()
        }

        binding.cardSettings.setSafeOnClickListener {
            selectViewModel.navigateToSettings()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
    }

    private fun navigate(navigation: SelectViewModel.Navigation) {
        (activity as SelectActivity).apply {
            when (navigation) {
                SelectViewModel.Navigation.Settings -> activity?.startActivity<SettingsActivity> {}
                SelectViewModel.Navigation.Info -> activity?.startActivity<InfoActivity> {}
                SelectViewModel.Navigation.SelectGame -> findNavController().navigate(SelectFragmentDirections.actionNavigationSelectToSelectGame())
            }
        }
    }
}
