package com.quiz.pride.ui.game

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.quiz.domain.Pride
import com.quiz.pride.R
import com.quiz.pride.common.startActivity
import com.quiz.pride.common.traslationAnimation
import com.quiz.pride.common.traslationAnimationFadeIn
import com.quiz.pride.databinding.GameFragmentBinding
import com.quiz.pride.ui.result.ResultActivity
import com.quiz.pride.utils.*
import com.quiz.pride.utils.Constants.POINTS
import com.quiz.pride.utils.Constants.TOTAL_PRIDES
import kotlinx.android.synthetic.main.dialog_extra_life.*
import kotlinx.android.synthetic.main.dialog_save_record.*
import kotlinx.coroutines.*
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel
import java.util.concurrent.TimeUnit


class GameFragment : Fragment() {
    private var extraLife = false
    private val gameViewModel: GameViewModel by lifecycleScope.viewModel(this)
    private lateinit var binding: GameFragmentBinding

    private lateinit var imageLoading: ImageView
    private lateinit var imageQuiz: ImageView
    private lateinit var btnOptionOne: TextView
    private lateinit var btnOptionTwo: TextView
    private lateinit var btnOptionThree: TextView
    private lateinit var btnOptionFour: TextView

    private var life: Int = 2
    private var stage: Int = 1
    private var points: Int = 0

    companion object {
        fun newInstance() = GameFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GameFragmentBinding.inflate(inflater)
        val root = binding.root

        imageLoading = root.findViewById(R.id.imagenLoading)
        imageQuiz = root.findViewById(R.id.imageQuiz)
        btnOptionOne = root.findViewById(R.id.btnOptionOne)
        btnOptionTwo = root.findViewById(R.id.btnOptionTwo)
        btnOptionThree = root.findViewById(R.id.btnOptionThree)
        btnOptionFour = root.findViewById(R.id.btnOptionFour)

        btnOptionOne.setSafeOnClickListener {
            btnOptionOne.isSelected = !btnOptionOne.isSelected
            checkResponse()
        }

        btnOptionTwo.setSafeOnClickListener {
            btnOptionTwo.isSelected = !btnOptionTwo.isSelected
            checkResponse()
        }

        btnOptionThree.setSafeOnClickListener {
            btnOptionThree.isSelected = !btnOptionThree.isSelected
            checkResponse()
        }

        btnOptionFour.setSafeOnClickListener {
            btnOptionFour.isSelected = !btnOptionFour.isSelected
            checkResponse()
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
        gameViewModel.question.observe(viewLifecycleOwner, Observer(::drawQuestionQuiz))
        gameViewModel.responseOptions.observe(viewLifecycleOwner, Observer(::drawOptionsResponse))
        gameViewModel.showingAds.observe(viewLifecycleOwner, Observer(::loadAdAndProgress))
        gameViewModel.progress.observe(viewLifecycleOwner, Observer(::loadAdAndProgress))
    }

    private fun loadAdAndProgress(model: GameViewModel.UiModel) {
        when(model) {
            is GameViewModel.UiModel.ShowBannerAd -> {
                (activity as GameActivity).showBannerAd(model.show)
            }
            is GameViewModel.UiModel.ShowReewardAd -> {
                (activity as GameActivity).showRewardedAd(model.show)
            }
            is GameViewModel.UiModel.Loading -> updateProgress(model.show)
        }
    }

    private fun navigate(navigation: GameViewModel.Navigation?) {
        when (navigation) {
            is GameViewModel.Navigation.Result -> {
                activity?.startActivity<ResultActivity> { putExtra(POINTS, points) }
            }
            is GameViewModel.Navigation.ExtraLifeDialog -> {
                showExtraLifeDialog()
            }
        }
    }

    private fun updateProgress(isShowing: Boolean) {
        if (isShowing) {
            glideLoadingGif(activity as GameActivity, imageLoading)
            imageLoading.visibility = View.VISIBLE
            imageQuiz.visibility = View.GONE

            btnOptionOne.isSelected = false
            btnOptionTwo.isSelected = false
            btnOptionThree.isSelected = false
            btnOptionFour.isSelected = false

            enableBtn(false)
        } else {
            imageLoading.visibility = View.GONE
            imageQuiz.visibility = View.VISIBLE

            enableBtn(true)
        }
    }

    private fun drawQuestionQuiz(pride: Pride) {
        glideLoadURL(activity as GameActivity, pride.flag, imageQuiz)
    }

    private fun drawOptionsResponse(optionsListByPos: MutableList<String>) {
        var delay = 150L
        if(stage == 1) {
            delay = 0L
            binding.containerButtons.traslationAnimationFadeIn()
        }
        else binding.containerButtons.traslationAnimation()

        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(delay))
            withContext(Dispatchers.Main) {
                btnOptionOne.text = optionsListByPos[0]
                btnOptionTwo.text = optionsListByPos[1]
                btnOptionThree.text = optionsListByPos[2]
                btnOptionFour.text = optionsListByPos[3]
            }
        }
    }

    private fun checkResponse() {
        enableBtn(false)
        stage += 1

        val name = gameViewModel.getPride().name
        val nameLocalize = when {
            getString(R.string.locale) == "es" -> name?.ES
            getString(R.string.locale) == "fr" -> name?.FR
            getString(R.string.locale) == "pt" -> name?.PT
            getString(R.string.locale) == "de" -> name?.DE
            getString(R.string.locale) == "it" -> name?.IT
            else -> name?.EN
        }

        drawCorrectResponse(nameLocalize!!)
        nextScreen()
    }

    private fun deleteLife() {
        life--
        (activity as GameActivity).writeLife(life)
    }

    private fun addExtraLife() {
        CoroutineScope(Dispatchers.IO).launch {
            if(life == 0) {
                delay(TimeUnit.MILLISECONDS.toMillis(2500))
                life = 1
                (activity as GameActivity).writeLife(1)
                points--
                (activity as GameActivity).writePoints(points)
                gameViewModel.generateNewStage()
            }
        }
    }

    private fun drawCorrectResponse(capitalNameCorrect: String) {
        when {
            btnOptionOne.text == capitalNameCorrect -> {
                btnOptionOne.background =  ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_correct)
                when {
                    btnOptionOne.isSelected -> {
                        soundSuccess()
                        points += 1
                        (activity as GameActivity).writePoints(points)
                    }
                    btnOptionTwo.isSelected -> {
                        soundFail()
                        deleteLife()
                        btnOptionTwo.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_wrong)
                    }
                    btnOptionThree.isSelected -> {
                        soundFail()
                        deleteLife()
                        btnOptionThree.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_wrong)
                    }
                    btnOptionFour.isSelected -> {
                        soundFail()
                        deleteLife()
                        btnOptionFour.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_wrong)
                    }
                    else -> {
                        soundFail()
                        deleteLife()
                    }
                }
            }
            btnOptionTwo.text == capitalNameCorrect -> {
                btnOptionTwo.background =  ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_correct)
                when {
                    btnOptionOne.isSelected -> {
                        soundFail()
                        deleteLife()
                        btnOptionOne.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_wrong)
                    }
                    btnOptionTwo.isSelected -> {
                        soundSuccess()
                        points += 1
                        (activity as GameActivity).writePoints(points)
                    }
                    btnOptionThree.isSelected -> {
                        soundFail()
                        deleteLife()
                        btnOptionThree.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_wrong)
                    }
                    btnOptionFour.isSelected -> {
                        soundFail()
                        deleteLife()
                        btnOptionFour.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_wrong)
                    }
                    else -> {
                        soundFail()
                        deleteLife()
                    }
                }
            }
            btnOptionThree.text == capitalNameCorrect -> {
                btnOptionThree.background =  ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_correct)
                when {
                    btnOptionOne.isSelected -> {
                        soundFail()
                        deleteLife()
                        btnOptionOne.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_wrong)
                    }
                    btnOptionTwo.isSelected -> {
                        soundFail()
                        deleteLife()
                        btnOptionTwo.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_wrong)
                    }
                    btnOptionThree.isSelected -> {
                        soundSuccess()
                        points += 1
                        (activity as GameActivity).writePoints(points)
                    }
                    btnOptionFour.isSelected -> {
                        soundFail()
                        deleteLife()
                        btnOptionFour.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_wrong)
                    }
                    else -> {
                        soundFail()
                        deleteLife()
                    }
                }
            }
            btnOptionFour.text == capitalNameCorrect -> {
                btnOptionFour.background =  ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_correct)
                when {
                    btnOptionOne.isSelected -> {
                        soundFail()
                        deleteLife()
                        btnOptionOne.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_wrong)
                    }
                    btnOptionTwo.isSelected -> {
                        soundFail()
                        deleteLife()
                        btnOptionTwo.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_wrong)
                    }
                    btnOptionThree.isSelected -> {
                        soundFail()
                        deleteLife()
                        btnOptionThree.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_radius_wrong)
                    }
                    btnOptionFour.isSelected -> {
                        soundSuccess()
                        points += 1
                        (activity as GameActivity).writePoints(points)
                    }
                    else -> {
                        soundFail()
                        deleteLife()
                    }
                }
            }
        }
    }

    private fun enableBtn(isEnable: Boolean) {
        btnOptionOne.isClickable = isEnable
        btnOptionTwo.isClickable = isEnable
        btnOptionThree.isClickable = isEnable
        btnOptionFour.isClickable = isEnable

        if(isEnable) {
            btnOptionOne.background = ContextCompat.getDrawable(requireContext(), R.drawable.selector_with_radius_button)
            btnOptionTwo.background = ContextCompat.getDrawable(requireContext(), R.drawable.selector_with_radius_button)
            btnOptionThree.background = ContextCompat.getDrawable(requireContext(), R.drawable.selector_with_radius_button)
            btnOptionFour.background = ContextCompat.getDrawable(requireContext(), R.drawable.selector_with_radius_button)
        }
    }

    private fun nextScreen() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(1000))
            withContext(Dispatchers.Main) {
                if(life < 1 && !extraLife && stage < TOTAL_PRIDES) {
                    extraLife = true
                    gameViewModel.navigateToExtraLifeDialog()
                } else if(stage > (TOTAL_PRIDES + 1) || life < 1) {
                    gameViewModel.navigateToResult(points.toString())
                } else {
                    gameViewModel.generateNewStage()
                    if(stage % 15 == 0) gameViewModel.showRewardedAd()
                }
            }
        }
    }

    private fun showExtraLifeDialog() {
        Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_extra_life)
            btnNo.setSafeOnClickListener {
                dismiss()
                gameViewModel.navigateToResult(points.toString())
            }
            btnYes.setSafeOnClickListener {
                dismiss()
                gameViewModel.showRewardedAd()
                addExtraLife()
            }
            show()
        }
    }

    private fun soundFail() {
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("sound", true)) {
            MediaPlayer.create(context, R.raw.fail).start()
        }
    }

    private fun soundSuccess() {
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("sound", true)) {
            MediaPlayer.create(context, R.raw.success).start()
        }
    }
}
