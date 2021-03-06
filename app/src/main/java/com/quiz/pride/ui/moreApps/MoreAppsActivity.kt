package com.quiz.pride.ui.moreApps

import android.os.Bundle
import android.view.View
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.utils.setSafeOnClickListener
import kotlinx.android.synthetic.main.app_bar_layout.*

class MoreAppsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.more_apps_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerResult, MoreAppsFragment.newInstance())
                .commitNow()
        }

        btnBack.setSafeOnClickListener {
            finish()
        }
        layoutExtendedTitle.background = null
        toolbarTitle.text = getString(R.string.more_apps)
        layoutLife.visibility = View.GONE
    }
}