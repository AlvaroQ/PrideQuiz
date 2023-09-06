package com.quiz.pride.ui.game

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.quiz.domain.Name
import com.quiz.domain.Pride
import com.quiz.pride.R
import com.quiz.pride.common.startActivity
import com.quiz.pride.databinding.DialogExtraLifeBinding
import com.quiz.pride.databinding.GameFragmentBinding
import com.quiz.pride.ui.result.ResultActivity
import com.quiz.pride.utils.Constants
import com.quiz.pride.utils.Constants.GameType.ADVANCE
import com.quiz.pride.utils.Constants.GameType.EXPERT
import com.quiz.pride.utils.Constants.GameType.NORMAL
import com.quiz.pride.utils.Constants.POINTS
import com.quiz.pride.utils.Constants.TOTAL_PRIDES
import com.quiz.pride.utils.glideLoadURL
import com.quiz.pride.utils.glideLoadingGif
import com.quiz.pride.utils.setBackground
import com.quiz.pride.utils.setSafeOnClickListener
import com.quiz.pride.utils.traslationAnimation
import com.quiz.pride.utils.traslationAnimationFadeIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit


class GameFragment : Fragment() {
    private val gameViewModel: GameViewModel by viewModel()
    private lateinit var binding: GameFragmentBinding

    private var extraLife = false
    private lateinit var question: Pride
    private lateinit var gameType: Constants.GameType

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

        gameType = activity?.intent?.extras?.get(Constants.GAME_TYPE) as Constants.GameType
        Log.d("GameActivity", "gameType: $gameType")

        initLayout()
        return root
    }

    private fun initLayout() {
        with(binding) {
            textQuiz.movementMethod = ScrollingMovementMethod()

            btnOptionOne.setSafeOnClickListener {
                layoutOptionOne.startAnimation(
                    AnimationUtils.loadAnimation(
                        context,
                        R.anim.scale_xy_collapse
                    )
                )
                btnOptionOne.isSelected = !btnOptionOne.isSelected
                checkResponse()
            }

            layoutOptionOne.setSafeOnClickListener {
                btnOptionOne.isSelected = !layoutOptionOne.isSelected
                checkResponse()
            }

            btnOptionTwo.setSafeOnClickListener {
                layoutOptionTwo.startAnimation(
                    AnimationUtils.loadAnimation(
                        context,
                        R.anim.scale_xy_collapse
                    )
                )
                btnOptionTwo.isSelected = !btnOptionTwo.isSelected
                checkResponse()
            }

            layoutOptionTwo.setSafeOnClickListener {
                btnOptionTwo.isSelected = !btnOptionTwo.isSelected
                checkResponse()
            }

            btnOptionThree.setSafeOnClickListener {
                layoutOptionThree.startAnimation(
                    AnimationUtils.loadAnimation(
                        context,
                        R.anim.scale_xy_collapse
                    )
                )
                btnOptionThree.isSelected = !btnOptionThree.isSelected
                checkResponse()
            }

            layoutOptionThree.setSafeOnClickListener {
                btnOptionThree.isSelected = !btnOptionThree.isSelected
                checkResponse()
            }

            btnOptionFour.setSafeOnClickListener {
                layoutOptionFour.startAnimation(
                    AnimationUtils.loadAnimation(
                        context,
                        R.anim.scale_xy_collapse
                    )
                )
                btnOptionFour.isSelected = !btnOptionFour.isSelected
                checkResponse()
            }

            layoutOptionFour.setSafeOnClickListener {
                btnOptionFour.isSelected = !btnOptionFour.isSelected
                checkResponse()
            }
        }
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

    private fun navigate(navigation: GameViewModel.Navigation) {
        when (navigation) {
            is GameViewModel.Navigation.Result -> {
                activity?.startActivity<ResultActivity> { putExtra(POINTS, points) }
            }
            is GameViewModel.Navigation.ExtraLifeDialog -> {
                showExtraLifeDialog()
                //gameViewModel.navigateToResult(points.toString())
            }
        }
    }

    private fun updateProgress(isShowing: Boolean) {
        with(binding) {
            if (isShowing) {
                glideLoadingGif(requireContext(), imagenLoading)
                imagenLoading.visibility = View.VISIBLE
                imageQuiz.visibility = View.GONE
                textQuiz.text = ""
                imageOptionOne.setImageResource(0)
                imageOptionTwo.setImageResource(0)
                imageOptionThree.setImageResource(0)
                imageOptionFour.setImageResource(0)

                btnOptionOne.isSelected = false
                btnOptionTwo.isSelected = false
                btnOptionThree.isSelected = false
                btnOptionFour.isSelected = false

                enableBtn(false)

                imageOptionOne.tag = "false"
                imageOptionTwo.tag = "false"
                imageOptionThree.tag = "false"
                imageOptionFour.tag = "false"
            } else {
                imagenLoading.visibility = View.GONE
                imageQuiz.visibility = View.VISIBLE
                textQuiz.visibility = View.VISIBLE

                enableBtn(true)
            }
        }
    }

    private fun drawQuestionQuiz(pride: Pride) {
        question = pride
        with(binding) {
            when (gameType) {
                NORMAL -> glideLoadURL(requireContext(), pride.flag, imageQuiz)
                ADVANCE -> textQuiz.text = getLocalizeName(pride.description!!)
                EXPERT -> textQuiz.text = getLocalizeName(pride.name!!)
            }
        }
    }

    private fun marckTagButtonAsCorrect(optionsListByPos: MutableList<Pride>) {
        with(binding) {
            btnOptionOne.tag = "false"
            btnOptionTwo.tag = "false"
            btnOptionThree.tag = "false"
            btnOptionFour.tag = "false"

            when (question.name) {
                optionsListByPos[0].name -> btnOptionOne.tag = "true"
                optionsListByPos[1].name -> btnOptionTwo.tag = "true"
                optionsListByPos[2].name -> btnOptionThree.tag = "true"
                optionsListByPos[3].name -> btnOptionFour.tag = "true"
            }
        }
    }

    private fun drawOptionsResponse(optionsListByPos: MutableList<Pride>) {
        if(stage == 1) {
            binding.containerButtons.traslationAnimationFadeIn()
        }
        else binding.containerButtons.traslationAnimation()

        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(150L))
            withContext(Dispatchers.Main) {
                when (gameType) {
                    NORMAL -> {
                        with(binding) {
                            btnOptionOne.text = getLocalizeName(optionsListByPos[0].name!!)
                            imageOptionOne.visibility = View.GONE
                            layoutOptionOne.background = ColorDrawable(Color.TRANSPARENT)

                            btnOptionTwo.text = getLocalizeName(optionsListByPos[1].name!!)
                            imageOptionTwo.visibility = View.GONE
                            layoutOptionTwo.background = ColorDrawable(Color.TRANSPARENT)

                            btnOptionThree.text = getLocalizeName(optionsListByPos[2].name!!)
                            imageOptionThree.visibility = View.GONE
                            layoutOptionThree.background = ColorDrawable(Color.TRANSPARENT)

                            btnOptionFour.text = getLocalizeName(optionsListByPos[3].name!!)
                            imageOptionFour.visibility = View.GONE
                            layoutOptionFour.background = ColorDrawable(Color.TRANSPARENT)
                        }

                        marckTagButtonAsCorrect(optionsListByPos)
                    }
                    else -> {
                        with(binding) {
                            glideLoadURL(
                                requireContext(),
                                optionsListByPos[0].flag,
                                imageOptionOne
                            )
                            btnOptionOne.visibility = View.GONE

                            glideLoadURL(
                                requireContext(),
                                optionsListByPos[1].flag,
                                imageOptionTwo
                            )
                            btnOptionTwo.visibility = View.GONE

                            glideLoadURL(
                                requireContext(),
                                optionsListByPos[2].flag,
                                imageOptionThree
                            )
                            btnOptionThree.visibility = View.GONE

                            glideLoadURL(
                                requireContext(),
                                optionsListByPos[3].flag,
                                imageOptionFour
                            )
                            btnOptionFour.visibility = View.GONE

                            marckTagButtonAsCorrect(optionsListByPos)
                        }
                    }
                }
            }
        }
    }

    private fun checkResponse() {
        enableBtn(false)
        stage += 1
        drawCorrectResponse()
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

    private fun successOption(){
        soundSuccess()
        points += 1
        (activity as GameActivity).writePoints(points)
    }
    private fun failOptionOne(){
        soundFail()
        deleteLife()

        with(binding) {
            btnOptionOne.setBackground(requireContext(), isCorrect = false)
            layoutOptionOne.setBackground(requireContext(), isCorrect = false)
        }
    }
    private fun failOptionTwo(){
        soundFail()
        deleteLife()
        with(binding) {
            btnOptionTwo.setBackground(requireContext(), isCorrect = false)
            layoutOptionTwo.setBackground(requireContext(), isCorrect = false)
        }
    }
    private fun failOptionThree(){
        soundFail()
        deleteLife()
        with(binding) {
            btnOptionThree.setBackground(requireContext(), isCorrect = false)
            layoutOptionThree.setBackground(requireContext(), isCorrect = false)
        }
    }
    private fun failOptionFour(){
        soundFail()
        deleteLife()
        with(binding) {
            btnOptionFour.setBackground(requireContext(), isCorrect = false)
            layoutOptionFour.setBackground(requireContext(), isCorrect = false)
        }
    }

    private fun drawCorrectResponse() {
        with(binding) {
            when {
                btnOptionOne.tag == "true" -> {
                    btnOptionOne.setBackground(requireContext(), isCorrect = true)
                    layoutOptionOne.setBackground(requireContext(), isCorrect = true)
                    when {
                        btnOptionOne.isSelected -> successOption()
                        btnOptionTwo.isSelected -> failOptionTwo()
                        btnOptionThree.isSelected -> failOptionThree()
                        btnOptionFour.isSelected -> failOptionFour()
                    }
                }
                btnOptionTwo.tag == "true" -> {
                    btnOptionTwo.setBackground(requireContext(), isCorrect = true)
                    layoutOptionTwo.setBackground(requireContext(), isCorrect = true)
                    when {
                        btnOptionOne.isSelected -> failOptionOne()
                        btnOptionTwo.isSelected -> successOption()
                        btnOptionThree.isSelected -> failOptionThree()
                        btnOptionFour.isSelected -> failOptionFour()
                    }
                }
                btnOptionThree.tag == "true" -> {
                    btnOptionThree.setBackground(requireContext(), isCorrect = true)
                    layoutOptionThree.setBackground(requireContext(), isCorrect = true)
                    when {
                        btnOptionOne.isSelected -> failOptionOne()
                        btnOptionTwo.isSelected -> failOptionTwo()
                        btnOptionThree.isSelected -> successOption()
                        btnOptionFour.isSelected -> failOptionFour()
                    }
                }
                btnOptionFour.tag == "true" -> {
                    btnOptionFour.setBackground(requireContext(), isCorrect = true)
                    layoutOptionFour.setBackground(requireContext(), isCorrect = true)
                    when {
                        btnOptionOne.isSelected -> failOptionOne()
                        btnOptionTwo.isSelected -> failOptionTwo()
                        btnOptionThree.isSelected -> failOptionThree()
                        btnOptionFour.isSelected -> successOption()
                    }
                }
            }
        }
    }

    private fun enableBtn(isEnable: Boolean) {
        with(binding) {
            btnOptionOne.isClickable = isEnable
            btnOptionTwo.isClickable = isEnable
            btnOptionThree.isClickable = isEnable
            btnOptionFour.isClickable = isEnable
            layoutOptionOne.isClickable = isEnable
            layoutOptionTwo.isClickable = isEnable
            layoutOptionThree.isClickable = isEnable
            layoutOptionFour.isClickable = isEnable

            if (isEnable) {
                when (gameType) {
                    NORMAL -> {
                        btnOptionOne.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.selector_with_radius_button
                        )
                        btnOptionTwo.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.selector_with_radius_button
                        )
                        btnOptionThree.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.selector_with_radius_button
                        )
                        btnOptionFour.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.selector_with_radius_button
                        )
                    }
                    else -> {
                        layoutOptionOne.background =
                            ContextCompat.getDrawable(requireContext(), R.color.white)
                        layoutOptionTwo.background =
                            ContextCompat.getDrawable(requireContext(), R.color.white)
                        layoutOptionThree.background =
                            ContextCompat.getDrawable(requireContext(), R.color.white)
                        layoutOptionFour.background =
                            ContextCompat.getDrawable(requireContext(), R.color.white)
                    }
                }
            }
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
                    if(stage != 0 && stage % 8 == 0) gameViewModel.showRewardedAd()
                }
            }
        }
    }

    private fun showExtraLifeDialog() {
        Dialog(requireContext()).apply {
            val binding = DialogExtraLifeBinding.inflate(layoutInflater)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)
            binding.btnNo.setSafeOnClickListener {
                dismiss()
                gameViewModel.navigateToResult(points.toString())
            }
            binding.btnYes.setSafeOnClickListener {
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

    private fun getLocalizeName(data: Name): String {
        return when {
            getString(R.string.locale) == "es" -> data.ES!!
            getString(R.string.locale) == "fr" -> data.FR!!
            getString(R.string.locale) == "pt" -> data.PT!!
            getString(R.string.locale) == "de" -> data.DE!!
            getString(R.string.locale) == "it" -> data.IT!!
            else -> data.EN!!
        }
    }
}
