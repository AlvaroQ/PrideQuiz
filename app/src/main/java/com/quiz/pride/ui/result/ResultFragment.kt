package com.quiz.pride.ui.result

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.quiz.domain.App
import com.quiz.domain.User
import com.quiz.pride.R
import com.quiz.pride.common.startActivity
import com.quiz.pride.databinding.ResultFragmentBinding
import com.quiz.pride.ui.ranking.RankingActivity
import com.quiz.pride.ui.settings.SettingsActivity
import com.quiz.pride.ui.settings.SettingsViewModel
import com.quiz.pride.utils.*
import com.quiz.pride.utils.Constants.POINTS
import kotlinx.android.synthetic.main.dialog_save_record.*
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel


class ResultFragment : Fragment() {
    private lateinit var binding: ResultFragmentBinding
    private val resultViewModel: ResultViewModel by lifecycleScope.viewModel(this)
    private var gamePoints = 0
    private lateinit var imageViewPickup: ImageView

    companion object {
        fun newInstance() = ResultFragment()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = ResultFragmentBinding.inflate(inflater)
        val root = binding.root

        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("sound", true)) {
            MediaPlayer.create(context, R.raw.game_over).start()
        }
        gamePoints = activity?.intent?.extras?.getInt(POINTS)!!

        val textResult: TextView = root.findViewById(R.id.textResult)
        textResult.text = resources.getString(R.string.result, gamePoints)

        resultViewModel.getPersonalRecord(gamePoints)
        resultViewModel.setPersonalRecordOnServer(gamePoints)

        val btnContinue: Button = root.findViewById(R.id.btnContinue)
        btnContinue.setSafeOnClickListener { resultViewModel.navigateToGame() }

        val btnShare: Button = root.findViewById(R.id.btnShare)
        btnShare.setSafeOnClickListener { resultViewModel.navigateToShare(gamePoints) }

        val btnRate: Button = root.findViewById(R.id.btnRate)
        btnRate.setSafeOnClickListener { resultViewModel.navigateToRate() }

        val btnRanking: Button = root.findViewById(R.id.btnRanking)
        btnRanking.setSafeOnClickListener { resultViewModel.navigateToRanking() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resultViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
        resultViewModel.progress.observe(viewLifecycleOwner, Observer(::updateProgress))
        resultViewModel.list.observe(viewLifecycleOwner, Observer(::fillAppList))
        resultViewModel.personalRecord.observe(viewLifecycleOwner, Observer(::fillPersonalRecord))
        resultViewModel.worldRecord.observe(viewLifecycleOwner, Observer(::fillWorldRecord))
        resultViewModel.photoUrl.observe(viewLifecycleOwner, Observer(::writeUserImage))
        resultViewModel.showingAds.observe(viewLifecycleOwner, Observer(::loadAd))
    }

    private fun loadAd(model: ResultViewModel.UiModel) {
        if (model is ResultViewModel.UiModel.ShowAd)
            (activity as ResultActivity).showAd(model.show)
    }

    private fun fillWorldRecord(recordWorldPoints: String) {
        binding.textWorldRecord.text = resources.getString(R.string.world_record, recordWorldPoints)
    }

    private fun fillPersonalRecord(points: String) {
        binding.textPersonalRecord.text = resources.getString(R.string.personal_record, points)
    }

    private fun fillAppList(appList: MutableList<App>) {
        binding.recyclerviewOtherApps.adapter = AppListAdapter(
            activity as ResultActivity,
            appList,
            resultViewModel::onAppClicked
        )
    }

    private fun updateProgress(model: ResultViewModel.UiModel?) {
        if (model is ResultViewModel.UiModel.Loading && model.show) {
            glideLoadingGif(activity as ResultActivity, binding.imagenLoading)
            binding.imagenLoading.visibility = View.VISIBLE
        } else {
            binding.imagenLoading.visibility = View.GONE
        }
    }

    private fun navigate(navigation: ResultViewModel.Navigation?) {
        when (navigation) {
            ResultViewModel.Navigation.Rate -> rateApp(requireContext())
            ResultViewModel.Navigation.Game -> activity?.finishAfterTransition()
            ResultViewModel.Navigation.Ranking -> activity?.startActivity<RankingActivity> {}
            is ResultViewModel.Navigation.Share -> shareApp(navigation.points, requireContext())
            is ResultViewModel.Navigation.Open -> openAppOnPlayStore(requireContext(), navigation.url)
            is ResultViewModel.Navigation.Dialog -> showEnterNameDialog(navigation.points)
            ResultViewModel.Navigation.PickerImage -> {
                ImagePicker.with(this)
                        .crop()
                        .compress(maxSize = 128)
                        .maxResultSize(width = 200, height = 400)
                        .start()
            }
            else -> {}
        }
    }

    private fun showEnterNameDialog(points: String) {
        Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_save_record)
            btnCancel.setSafeOnClickListener { dismiss() }
            btnSubmit.setSafeOnClickListener {
                val userImage: String = if(resultViewModel.photoUrl.value.isNullOrEmpty()) Constants.DEFAULT_IMAGE else resultViewModel.photoUrl.value!!
                resultViewModel.saveTopScore(User(
                    name = editTextWorldRecord.text.toString(),
                    points = gamePoints.toString(),
                    score = gamePoints,
                    userImage = userImage,
                    timestamp = System.currentTimeMillis())
                )
                dismiss()
            }

            imageViewPickup = imageUserPickup
            imageViewPickup.setSafeOnClickListener { resultViewModel.clickOnPicker() }
            show()
        }
    }

    private fun writeUserImage(image64: String) {
        glideLoadBase64(requireContext(), image64, imageViewPickup)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                resultViewModel.setImage(ImagePicker.getFile(data)?.toBase64())
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(activity, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(activity, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
