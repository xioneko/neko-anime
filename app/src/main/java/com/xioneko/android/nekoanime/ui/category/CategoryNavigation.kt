package com.xioneko.android.nekoanime.ui.category

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.xioneko.android.nekoanime.data.model.AnimeCategory
import com.xioneko.android.nekoanime.data.model.labelValueOf

const val CategoryNavRoute = "category_route"

fun NavGraphBuilder.categoryScreen(
    onAnimeClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    composable(
        route = "$CategoryNavRoute/?type={type}&year={year}&genre={genre}&order={order}",
        arguments = buildList {
            AnimeCategory.entries.forEach { category ->
                add(navArgument(category.toString().lowercase()) {})
            }
        },
    ) { backStackEntry ->
        CategoryScreen(
            filter = buildMap {
                AnimeCategory.entries.forEach { category ->
                    val value =
                        backStackEntry.arguments?.getString(category.toString().lowercase()) ?: ""
                    // TODO: 处理 value 不合法的情况
                    put(category, value to category.labelValueOf(value))
                }
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
    navigate(
        "$CategoryNavRoute/?type=$type&year=$year&genre=$genre&order=$orderBy",
        navOptions { launchSingleTop = true })
}