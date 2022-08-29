package com.quiz.pride.ui.result

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.common.startActivity
import com.quiz.pride.common.viewBinding
import com.quiz.pride.databinding.ResultActivityBinding
import com.quiz.pride.ui.select.SelectActivity
import com.quiz.pride.utils.setSafeOnClickListener

class ResultActivity : BaseActivity() {
    private lateinit var activity: Activity

    private val binding by viewBinding(ResultActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerResult, ResultFragment.newInstance())
                .commitNow()
        }
        activity = this

        with(binding.appBar) {
            btnBack.setSafeOnClickListener {
                startActivity<SelectActivity> {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            toolbarTitle.text = getString(R.string.resultado_screen_title)
            layoutLife.visibility = View.GONE
        }
    }
}