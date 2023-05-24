package com.xioneko.android.nekoanime.ui.search

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.ui.component.AnimatedFollowIcon
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.neutral03
import com.xioneko.android.nekoanime.ui.theme.neutral08
import com.xioneko.android.nekoanime.ui.theme.pink30

private const val MAX_OVERFLOW_SIZE = 10

@Composable
fun SearchResult(
    modifier: Modifier,
    anime: Anime,
    isFollowed: Boolean,
    onClick: (Int) -> Unit,
    onFollowAnime: (Anime) -> Unit,
    onUnfollowAnime: (Anime) -> Unit,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 15.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = { onClick(anime.id) }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .size(60.dp, 82.dp)
                .clip(MaterialTheme.shapes.small),
            model = ImageRequest.Builder(LocalContext.current)
                .data(anime.imageUrl)
                .build(),
            contentDescription = null,
            placeholder = null,
            error = null,
            fallback = null,
            onLoading = null,
            onSuccess = null,
            onError = null,
        )
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = anime.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InfoTag(text = anime.year.toString(), iconId = NekoAnimeIcons.date2)
                        InfoTag(text = anime.status, iconId = NekoAnimeIcons.status)
                        InfoTag(
                            text = "第${anime.latestEpisode}话",
                            iconId = NekoAnimeIcons.episode
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .width(48.dp)
                        .padding(top = 6.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Switch,
                            onClick = {
                                if (isFollowed) onUnfollowAnime(anime)
                                else onFollowAnime(anime)
                            }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedFollowIcon(
                        modifier = Modifier.scale(1.5f),
                        isFollowed = isFollowed
                    )

                    Text(
                        text = if (isFollowed) "已追番" else "追番",
                        color = pink30,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            GenresRow(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 7.dp),
                spacing = 6.dp,
            ) {
                for (genre in anime.genres + List(MAX_OVERFLOW_SIZE) { "+$it" }) {
                    GenreTag(text = genre)
                }
            }
        }
    }
}


/**
 * 风格标签过多导致溢出时，补充一个额外标签（显示溢出个数）
 */
@Composable
fun GenresRow(
    modifier: Modifier,
    spacing: Dp,
    content: @Composable () -> Unit
) {
    Layout(content, modifier) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val genreTags = placeables.subList(0, placeables.size - MAX_OVERFLOW_SIZE)
        val overflowTags = placeables.subList(placeables.size - MAX_OVERFLOW_SIZE, placeables.size)

        val totalWidth = genreTags.fold(0) { acc, placeable -> acc + placeable.width }

        val overflow =
            totalWidth + (genreTags.size - 1) * spacing.roundToPx() > constraints.maxWidth
        var overflowCount = 0


        layout(constraints.maxWidth, placeables[0].height) {
            var xPos = 0
            val maxWidthWhenOverflow = constraints.maxWidth - (spacing + 32.dp).roundToPx()

            genreTags.forEach { placeable ->
                if (overflow && xPos + placeable.width > maxWidthWhenOverflow) {
                    overflowCount++
                    return@forEach
                }
                placeable.placeRelative(xPos, 0)
                xPos += placeable.width + spacing.roundToPx()
            }

            if (overflow) overflowTags[overflowCount].placeRelative(xPos, 0)
        }
    }
}


@Composable
private fun GenreTag(
    modifier: Modifier = Modifier,
    text: String,
) {
    Box(
        modifier = modifier
            .height(20.dp)
            .widthIn(min = 32.dp)
            .border(1.dp, neutral03, CircleShape)
            .padding(8.dp, 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = neutral08,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun InfoTag(
    text: String,
    iconId: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Icon(
            modifier = Modifier.size(14.dp),
            painter = painterResource(iconId),
            contentDescription = null,
            tint = neutral08
        )
        Text(
            text = text,
            color = neutral08,
            style = MaterialTheme.typography.labelSmall
        )
    }
}