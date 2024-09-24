package com.xioneko.android.nekoanime.ui.category

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.xioneko.android.nekoanime.data.model.AnimeCategory
import com.xioneko.android.nekoanime.data.model.pairValueOf
import kotlinx.serialization.Serializable

@Serializable
data class CategoryNavRoute(
    val type: String,
    val year: String,
    val genre: String,
    val orderBy: String,
)

fun NavGraphBuilder.categoryScreen(
    onAnimeClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    composable<CategoryNavRoute> { backStackEntry ->
        val navRoute = backStackEntry.toRoute<CategoryNavRoute>()
        CategoryScreen(
            filter = buildMap {
                put(AnimeCategory.Type, AnimeCategory.Type.pairValueOf(navRoute.type))
                put(AnimeCategory.Year, AnimeCategory.Year.pairValueOf(navRoute.year))
                put(AnimeCategory.Genre, AnimeCategory.Genre.pairValueOf(navRoute.genre))
                put(AnimeCategory.Order, AnimeCategory.Order.pairValueOf(navRoute.orderBy))
            },
            onAnimeClick = onAnimeClick,
            onSearchClick = onSearchClick,
            onBackClick = onBackClick
        )
    }
}

fun NavHostController.navigateToCategory(
    type: Int = 1,
    year: String = "",
    genre: String = "",
    orderBy: String = "time",
) {
    navigate(CategoryNavRoute(type.toString(), year, genre, orderBy))
}