package com.quiz.pride.ui.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.quiz.pride.R
import com.quiz.pride.common.startActivity
import com.quiz.pride.databinding.SelectFragmentBinding
import com.quiz.pride.ui.game.GameActivity
import com.quiz.pride.ui.ranking.RankingActivity
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

        val btnStart: Button = root.findViewById(R.id.btnStart)
        btnStart.setSafeOnClickListener {
            selectViewModel.navigateToGame()
        }

        val btnInfo: Button = root.findViewById(R.id.btnInfo)
        btnInfo.setSafeOnClickListener {
            selectViewModel.navigateToInfo()
        }

        val btnRanking: Button = root.findViewById(R.id.btnRanking)
        btnRanking.setSafeOnClickListener {
            selectViewModel.navigateToRanking()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
    }

    private fun navigate(navigation: SelectViewModel.Navigation?) {
        when (navigation) {
            SelectViewModel.Navigation.Game -> activity?.startActivity<GameActivity> {}
            SelectViewModel.Navigation.Info -> { }
            SelectViewModel.Navigation.Ranking -> activity?.startActivity<RankingActivity> {}
        }
    }
}
