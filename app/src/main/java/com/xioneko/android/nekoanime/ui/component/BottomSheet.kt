package com.xioneko.android.nekoanime.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.neutral02
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    skipPartiallyExpanded: Boolean = false,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.(requestDismiss: (onComplete: () -> Unit) -> Unit) -> Unit
) {
    val bottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        containerColor = basicWhite,
        contentColor = basicBlack,
        scrimColor = basicBlack.copy(alpha = 0.32f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = neutral02)
        },
        windowInsets = WindowInsets(0),
        sheetState = bottomSheetState,
        onDismissRequest = onDismiss,
        content = {
            content { onComplete ->
                scope.launch { bottomSheetState.hide() }
                    .invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            onDismiss()
                            onComplete()
                        }
                    }
            }
        },
    )
}