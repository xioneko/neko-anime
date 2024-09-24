package com.xioneko.android.nekoanime.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.xioneko.android.nekoanime.ui.component.LazyAnimeGrid
import com.xioneko.android.nekoanime.ui.component.shimmerBrush
import com.xioneko.android.nekoanime.ui.theme.brightNeutral03
import com.xioneko.android.nekoanime.ui.theme.brightNeutral04
import com.xioneko.android.nekoanime.ui.theme.brightNeutral05
import com.xioneko.android.nekoanime.ui.theme.brightNeutral06
import com.xioneko.android.nekoanime.ui.theme.brightNeutral08

@Composable
fun AnimePlayBodySkeleton() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brightNeutral03)
                .padding(12.dp, 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        Modifier
                            .size(132.dp, 22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                shimmerBrush(
                                    x = 132.dp,
                                    y = 22.dp,
                                    color = brightNeutral08
                                )
                            )
                    )
                    Box(
                        Modifier
                            .size(111.dp, 12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                shimmerBrush(
                                    x = 132.dp,
                                    y = 22.dp,
                                    color = brightNeutral08
                                )
                            )
                    )
                }
                Box(
                    Modifier
                        .size(54.dp, 30.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            shimmerBrush(
                                x = 34.dp,
                                y = 30.dp,
                                color = brightNeutral08
                            )
                        )
                )
            }
        }
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp, 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    repeat(2) {
                        Box(
                            Modifier
                                .size(120.dp, 26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    shimmerBrush(
                                        x = 120.dp,
                                        y = 26.dp,
                                        color = brightNeutral05
                                    )
                                )
                        )
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush(x = 400.dp, y = 26.dp, color = brightNeutral05))
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(83.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush(x = 400.dp, y = 83.dp, color = brightNeutral03))
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp, 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .size(128.dp, 20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush(x = 128.dp, y = 20.dp, color = brightNeutral06))
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(6) {
                        Box(
                            Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    shimmerBrush(
                                        x = 50.dp,
                                        y = 50.dp,
                                        color = brightNeutral04
                                    )
                                )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .padding(horizontal = 12.dp)
                        .size(128.dp, 20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush(x = 128.dp, y = 20.dp, color = brightNeutral06))
                )
                LazyAnimeGrid(
                    modifier = Modifier.requiredHeightIn(max = 960.dp),
                    useExpandCardStyle = false,
                    animeList = List(6) { null },
                    onAnimeClick = {}
                )
            }

        }
    }
}