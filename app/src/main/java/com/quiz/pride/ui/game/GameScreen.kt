package com.quiz.pride.ui.game

import android.media.MediaPlayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.quiz.domain.Pride
import com.quiz.pride.R
import com.quiz.pride.managers.ThemeManager
import com.quiz.pride.ui.components.GameTopBar
import com.quiz.pride.ui.components.LoadingIndicator
import com.quiz.pride.ui.theme.GradientGameBottom
import com.quiz.pride.ui.theme.GradientGameTop
import com.quiz.pride.ui.theme.ResponseCorrect
import com.quiz.pride.ui.theme.ResponseFail
import com.quiz.pride.ui.theme.White
import com.quiz.pride.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun GameScreen(
    gameType: Constants.GameType,
    onNavigateToResult: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val themeManager: ThemeManager = koinInject()
    val soundEnabled by themeManager.isSoundEnabled.collectAsState(initial = true)
    val coroutineScope = rememberCoroutineScope()

    // Game state
    var points by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(2) }
    var stage by remember { mutableIntStateOf(1) }
    var extraLifeUsed by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var showExtraLifeDialog by remember { mutableStateOf(false) }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is GameEvent.NavigateToResult -> onNavigateToResult(points)
                is GameEvent.ShowExtraLifeDialog -> showExtraLifeDialog = true
                is GameEvent.PlaySuccessSound -> {
                    if (soundEnabled) {
                        try {
                            MediaPlayer.create(context, R.raw.success)?.apply {
                                start()
                                setOnCompletionListener { release() }
                            }
                        } catch (_: Exception) {}
                    }
                }
                is GameEvent.PlayFailSound -> {
                    if (soundEnabled) {
                        try {
                            MediaPlayer.create(context, R.raw.fail)?.apply {
                                start()
                                setOnCompletionListener { release() }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            GameTopBar(
                points = points,
                lives = lives,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                LoadingIndicator()
            } else {
                GameContent(
                    gameType = gameType,
                    question = uiState.question,
                    options = uiState.options,
                    correctOptionIndex = uiState.correctOptionIndex,
                    selectedAnswer = selectedAnswer,
                    onAnswerSelected = { index ->
                        if (selectedAnswer == null) {
                            selectedAnswer = index
                            val isCorrect = index == uiState.correctOptionIndex

                            if (isCorrect) {
                                viewModel.playSuccessSound()
                                points++
                            } else {
                                viewModel.playFailSound()
                                lives--
                            }

                            // Delay before next question
                            coroutineScope.launch {
                                delay(1000)
                                selectedAnswer = null

                                when {
                                    lives < 1 && !extraLifeUsed && stage < Constants.TOTAL_PRIDES -> {
                                        extraLifeUsed = true
                                        viewModel.navigateToExtraLifeDialog()
                                    }
                                    stage >= Constants.TOTAL_PRIDES || lives < 1 -> {
                                        viewModel.navigateToResult(points.toString())
                                    }
                                    else -> {
                                        stage++
                                        viewModel.generateNewStage()
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    // Extra Life Dialog
    if (showExtraLifeDialog) {
        ExtraLifeDialog(
            onAccept = {
                showExtraLifeDialog = false
                lives = 1
                if (points > 0) points--
                viewModel.generateNewStage()
            },
            onDecline = {
                showExtraLifeDialog = false
                onNavigateToResult(points)
            }
        )
    }
}

@Composable
private fun GameContent(
    gameType: Constants.GameType,
    question: Pride?,
    options: List<Pride>,
    correctOptionIndex: Int,
    selectedAnswer: Int?,
    onAnswerSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Question area (35%)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f)
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (gameType) {
                Constants.GameType.NORMAL -> {
                    // Show flag image
                    AsyncImage(
                        model = question?.flag,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Constants.GameType.ADVANCE -> {
                    // Show description
                    Text(
                        text = question?.description?.EN ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
                Constants.GameType.EXPERT -> {
                    // Show name only
                    Text(
                        text = stringResource(R.string.expert_question),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Answer options area (65%)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f)
                .background(
                    Brush.verticalGradient(
                        listOf(GradientGameTop, GradientGameBottom)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // First row of options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnswerButton(
                        option = options.getOrNull(0),
                        gameType = gameType,
                        isSelected = selectedAnswer == 0,
                        isCorrect = selectedAnswer != null && correctOptionIndex == 0,
                        isWrong = selectedAnswer == 0 && correctOptionIndex != 0,
                        enabled = selectedAnswer == null,
                        onClick = { onAnswerSelected(0) },
                        modifier = Modifier.weight(1f)
                    )
                    AnswerButton(
                        option = options.getOrNull(1),
                        gameType = gameType,
                        isSelected = selectedAnswer == 1,
                        isCorrect = selectedAnswer != null && correctOptionIndex == 1,
                        isWrong = selectedAnswer == 1 && correctOptionIndex != 1,
                        enabled = selectedAnswer == null,
                        onClick = { onAnswerSelected(1) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Second row of options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnswerButton(
                        option = options.getOrNull(2),
                        gameType = gameType,
                        isSelected = selectedAnswer == 2,
                        isCorrect = selectedAnswer != null && correctOptionIndex == 2,
                        isWrong = selectedAnswer == 2 && correctOptionIndex != 2,
                        enabled = selectedAnswer == null,
                        onClick = { onAnswerSelected(2) },
                        modifier = Modifier.weight(1f)
                    )
                    AnswerButton(
                        option = options.getOrNull(3),
                        gameType = gameType,
                        isSelected = selectedAnswer == 3,
                        isCorrect = selectedAnswer != null && correctOptionIndex == 3,
                        isWrong = selectedAnswer == 3 && correctOptionIndex != 3,
                        enabled = selectedAnswer == null,
                        onClick = { onAnswerSelected(3) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerButton(
    option: Pride?,
    gameType: Constants.GameType,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = spring(),
        label = "answer_scale"
    )

    val backgroundColor = when {
        isCorrect -> ResponseCorrect
        isWrong -> ResponseFail
        else -> White
    }

    Card(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        enabled = enabled
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            when (gameType) {
                Constants.GameType.NORMAL -> {
                    // Show name for normal mode
                    Text(
                        text = option?.name?.EN ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = if (isCorrect || isWrong) White else Color.Black
                    )
                }
                else -> {
                    // Show flag for advance/expert modes
                    AsyncImage(
                        model = option?.flag,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtraLifeDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Cannot dismiss */ },
        title = {
            Text(
                text = stringResource(R.string.dialog_extra_life_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_extra_life_description),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(stringResource(R.string.dialog_extra_life_btn_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(stringResource(R.string.dialog_extra_life_btn_no))
            }
        }
    )
}
