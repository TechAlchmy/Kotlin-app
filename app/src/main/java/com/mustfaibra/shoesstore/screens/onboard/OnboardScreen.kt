package com.mustfaibra.shoesstore.screens.onboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mustfaibra.shoesstore.R
import com.mustfaibra.shoesstore.components.CustomButton
import com.mustfaibra.shoesstore.providers.LocalNavHost
import com.mustfaibra.shoesstore.sealed.Orientation
import com.mustfaibra.shoesstore.sealed.Screen
import com.mustfaibra.shoesstore.ui.theme.Dimension
import com.mustfaibra.shoesstore.utils.addMoveAnimation

@Composable
fun OnboardScreen(onboardViewModel: OnboardViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val controller = LocalNavHost.current
        val composition by rememberLottieComposition(
            spec = LottieCompositionSpec.RawRes(R.raw.walking_shoes),
        )
        // to control the animation speed
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever,
            speed = 2f,
            restartOnPlay = true,

        )

        LottieAnimation(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            progress = progress,
            composition = composition,
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .padding(Dimension.pagePadding),
            verticalArrangement = Arrangement.spacedBy(Dimension.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

//            val countDownTimer = object : CountDownTimer(2000,200){
//                override fun onTick(p0: Long) {
//
//                }
//                override fun onFinish() {
//                    TODO("Not yet implemented")
//                }
//            }
            Text(
                modifier = Modifier
                    .addMoveAnimation(
                        orientation = Orientation.Vertical,
                        from = (-100).dp,
                        to = 0.dp,
                        duration = 1000,
                    ),
                text = buildAnnotatedString {
                    append("Let's improve your")
                    withStyle(
                        style = MaterialTheme.typography.h3
                            .copy(color = MaterialTheme.colors.primary)
                            .toSpanStyle()
                    ) {
                        append(" appearance!")
                    }
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h3.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Text(
                modifier = Modifier
                    .addMoveAnimation(
                        orientation = Orientation.Vertical,
                        from = 100.dp,
                        to = 0.dp,
                        duration = 1000,
                    ),
                text = "Find cool shoes that make you feel more comfortable during you daily activities.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(modifier = Modifier.height(Dimension.pagePadding))
            CustomButton(
                modifier = Modifier
                    .addMoveAnimation(
                        orientation = Orientation.Vertical,
                        from = 300.dp,
                        to = 0.dp,
                        duration = 1000,
                    )
                    .padding(
                        horizontal = Dimension.pagePadding.times(2),
                        vertical = Dimension.pagePadding,
                    ),
                text = stringResource(R.string.get_started),
                buttonColor = MaterialTheme.colors.primary,
                shape = MaterialTheme.shapes.large,
                padding = PaddingValues(
                    vertical = Dimension.sm,
                    horizontal = Dimension.pagePadding.times(2f)
                ),
                onButtonClicked = {
                    /** First we should update app launched flag */
//                    onboardViewModel.updateLaunchState(context = context)
                    controller.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboard.route) {
                            inclusive = true
                        }
                    }
                },
                contentColor = MaterialTheme.colors.onPrimary,
            )
        }
    }
}