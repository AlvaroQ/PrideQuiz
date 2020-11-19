package com.quiz.pride.ui.ranking

import android.os.Bundle
import android.view.View
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.utils.setSafeOnClickListener
import kotlinx.android.synthetic.main.app_bar_layout.*

class RankingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerResult, RankingFragment.newInstance())
                .commitNow()
        }

        btnBack.setSafeOnClickListener {
            finish()
        }
        layoutExtendedTitle.background = getDrawable(R.drawable.background_title_top_score)
        toolbarTitle.text = getString(R.string.best_points)
        layoutLife.visibility = View.GONE
    }
}