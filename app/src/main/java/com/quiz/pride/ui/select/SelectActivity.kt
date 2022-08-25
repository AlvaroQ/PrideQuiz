package com.quiz.pride.ui.select

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.common.viewBinding
import com.quiz.pride.databinding.SelectActivityBinding

class SelectActivity : BaseActivity() {
    private val binding by viewBinding(SelectActivityBinding::inflate)
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        navController = findNavController(R.id.nav_host_fragment)
    }

    override fun onResume() {
        super.onResume()
        try {
            findNavController(R.id.nav_host_fragment).navigate(R.id.action_navigation_select_game_to_select)
        } catch (e:IllegalArgumentException) {}
    }

    override fun onBackPressed() {
        when (navController.currentDestination?.id) {
            R.id.navigation_select -> finish()
            R.id.navigation_select_game -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.action_navigation_select_game_to_select)
            }
        }
    }
}