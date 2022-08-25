package com.quiz.pride.ui.select

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.quiz.pride.R
import com.quiz.pride.common.startActivity
import com.quiz.pride.databinding.SelectGameFragmentBinding
import com.quiz.pride.ui.game.GameActivity
import com.quiz.pride.utils.Constants
import com.quiz.pride.utils.setSafeOnClickListener
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel


class SelectGameFragment : Fragment() {
    private lateinit var binding: SelectGameFragmentBinding
    private val selectGameViewModel: SelectGameViewModel by lifecycleScope.viewModel(this)

    companion object {
        fun newInstance() = SelectGameFragment()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = SelectGameFragmentBinding.inflate(inflater)
        val root = binding.root

        val cardNormal: CardView = root.findViewById(R.id.cardNormal)
        cardNormal.setSafeOnClickListener {
            selectGameViewModel.navigateToGame(Constants.GameType.NORMAL)
        }

        val cardAdvance: CardView = root.findViewById(R.id.cardAdvance)
        cardAdvance.setSafeOnClickListener {
            selectGameViewModel.navigateToGame(Constants.GameType.ADVANCE)
        }

        val cardExpert: CardView = root.findViewById(R.id.cardExpert)
        cardExpert.setSafeOnClickListener {
            selectGameViewModel.navigateToGame(Constants.GameType.EXPERT)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectGameViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
    }

    private fun navigate(navigation: SelectGameViewModel.Navigation) {
        when (navigation) {
            is SelectGameViewModel.Navigation.Game -> {
                activity?.startActivity<GameActivity> { putExtra(Constants.GAME_TYPE, navigation.type) }
                //findNavController().navigate(SelectGameFragmentDirections.actionNavigationSelectGameToSelect())
            }
        }
    }
}
