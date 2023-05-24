package com.xioneko.android.nekoanime.ui.search.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.neutral03
import com.xioneko.android.nekoanime.ui.theme.pink40
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

@Composable
internal fun CandidatesView(
    input: String,
    fetch: (String) -> Flow<String>,
    onCandidateClick: (String) -> Unit,
) {

    val candidateList = remember(input) {
        mutableStateListOf<Pair<MutableTransitionState<Boolean>, String>>()
    }

    LaunchedEffect(input) {
        fetch(input)
            .take(12)
            .onEach { candidate ->
                val visible = MutableTransitionState(false).apply { targetState = true }
                candidateList.add(visible to candidate)
            }
            .collect()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(basicWhite)
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        for (candidate in candidateList) {
            AnimatedVisibility(visibleState = candidate.first, enter = fadeIn(), exit = fadeOut()) {
                CandidateItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.DropdownList,
                            onClick = { onCandidateClick(candidate.second) }
                        ),
                    text = candidate.second,
                    keyword = input
                )
            }
        }
    }
}

@Composable
private fun CandidateItem(modifier: Modifier, text: String, keyword: String) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(NekoAnimeIcons.search),
            contentDescription = null,
            tint = neutral03
        )
        Text(
            text = text.highlightKeyword(keyword),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun String.highlightKeyword(keyword: String): AnnotatedString {
    val index = indexOf(keyword)
    if (index == -1) return AnnotatedString(this)

    return buildAnnotatedString {
        append(substring(0, index))
        pushStyle(SpanStyle(color = pink40))
        append(keyword)
        pop()
        append(substring(index + keyword.length))
        toAnnotatedString()
    }
}