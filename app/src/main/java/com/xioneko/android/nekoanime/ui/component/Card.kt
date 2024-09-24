package com.xioneko.android.nekoanime.ui.component

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.xioneko.android.nekoanime.R
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.model.asAnimeShell
import com.xioneko.android.nekoanime.domain.model.FollowedAnime
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.brightNeutral04
import com.xioneko.android.nekoanime.ui.theme.brightNeutral05
import com.xioneko.android.nekoanime.ui.theme.brightNeutral06
import com.xioneko.android.nekoanime.ui.theme.darkPink60
import com.xioneko.android.nekoanime.ui.theme.pink40

@Composable
fun NarrowAnimeCard(
    modifier: Modifier = Modifier,
    anime: Anime,
    subTitle: String = anime.status,
    onClick: (Int) -> Unit
) = NarrowAnimeCard(modifier, anime.asAnimeShell(), subTitle, onClick)


@Composable
fun NarrowAnimeCard(
    modifier: Modifier = Modifier,
    anime: AnimeShell,
    subTitle: String = anime.status,
    onClick: (Int) -> Unit
) {
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = { onClick(anime.id) }
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.clip(MaterialTheme.shapes.extraSmall)) {
            AnimeImage(
                modifier = Modifier.aspectRatio(0.72f),
                imageUrl = anime.imageUrl
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                        )
                    ),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    modifier = Modifier.offset((-6).dp, (-5).dp),
                    text = subTitle,
                    color = basicWhite,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        Text(
            text = anime.name,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = basicBlack,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Composable
fun ExpandedAnimeCard(
    modifier: Modifier = Modifier,
    anime: AnimeShell,
    subTitle: String = anime.status,
    onClick: (Int) -> Unit,
) {
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = { onClick(anime.id) }
        ),
    ) {
        AnimeImage(
            modifier = Modifier.aspectRatio(0.72f),
            imageUrl = anime.imageUrl
        )
        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = anime.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = basicBlack,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = subTitle,
            color = darkPink60,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun PlaceholderAnimeCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(shimmerBrush(Offset(x = 400f, y = 400f / 0.72f), brightNeutral04))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(12.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(shimmerBrush(Offset(x = 400f, y = 100f), brightNeutral05))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(12.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(shimmerBrush(Offset(x = 400f, y = 200f), brightNeutral06))
        )
    }
}


@Composable
fun shimmerBrush(x: Dp, y: Dp, color: Color, durationMillis: Int = 1000): Brush =
    with(LocalDensity.current) {
        shimmerBrush(Offset(x.toPx(), y.toPx()), color, durationMillis)
    }

@Composable
fun shimmerBrush(
    targetOffset: Offset,
    color: Color,
    durationMillis: Int = 1000
): Brush {
    val shimmerColors = listOf(
        color,
        color.increaseLuminanceBy(0.1f),
        color,
    )
    val transition = rememberInfiniteTransition(label = "")

    val ratio = targetOffset.x / targetOffset.y
    val horizontalDist = 350f
    val verticalDist = horizontalDist / ratio

    val startX by transition.animateFloat(
        initialValue = 0f - horizontalDist,
        targetValue = targetOffset.x,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis)), label = ""
    )
    val startY by transition.animateFloat(
        initialValue = 0f - verticalDist,
        targetValue = targetOffset.y,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis)), label = ""
    )

    val endX by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetOffset.x + horizontalDist,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis)), label = ""
    )
    val endY by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetOffset.y + verticalDist,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis)), label = ""
    )


    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = startX, y = startY),
        end = Offset(x = endX, y = endY)
    )
}

private fun Color.increaseLuminanceBy(factor: Float) =
    Color(
        alpha = alpha,
        red = (red * (1 + factor)).coerceIn(0f, 255f),
        green = (green * (1 + factor)).coerceIn(0f, 255f),
        blue = (blue * (1 + factor)).coerceIn(0f, 255f)
    )


@Composable
fun FollowedAnimeCard(
    modifier: Modifier = Modifier,
    anime: FollowedAnime,
    onClick: (Int) -> Unit,
) {
    if (anime.name == null) {
        PlaceholderAnimeCard(modifier)
    } else {
        Column(
            modifier = modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = { onClick(anime.id) }
            ),
        ) {
            AnimeImage(
                modifier = Modifier.aspectRatio(0.72f),
                imageUrl = anime.imageUrl
            )
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = anime.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = basicBlack,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (anime.isFinished!!) "已看完"
                else if (anime.currentEpisode == 0) "尚未观看"
                else "看到第${anime.currentEpisode}话",
                color = pink40,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun AnimeImage(modifier: Modifier = Modifier, imageUrl: String?) {
    AsyncImage(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall),
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentScale = ContentScale.Crop,
        contentDescription = null,
        placeholder = painterResource(R.drawable.broken_image),
        error = painterResource(R.drawable.broken_image),
    )
}