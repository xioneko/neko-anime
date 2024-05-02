package com.xioneko.android.nekoanime.ui.category

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.xioneko.android.nekoanime.data.model.Category
import com.xioneko.android.nekoanime.data.model.labelValueOf

const val CategoryNavRoute = "category_route"

fun NavGraphBuilder.categoryScreen(
    onAnimeClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    composable(
        route =
        "$CategoryNavRoute/?region={region}&type={type}&year={year}&quarter={quarter}&status={status}&genre={genre}&order={order}",
        arguments = buildList {
            Category.values().forEach { category ->
                add(navArgument(category.toString().lowercase()) {})
            }
        },
    ) { backStackEntry ->
        CategoryScreen(
            filter = buildMap {
                Category.values().forEach { category ->
                    val value = backStackEntry.arguments?.getString(category.toString().lowercase()) ?: ""
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
    region: String = "日本",
    type: String = "",
    year: String = "",
    quarter: String = "",
    status: String = "",
    genre: String = "",
    orderBy: String = "",
) {
    navigate(
        "$CategoryNavRoute/?region=$region&type=$type&year=$year&quarter=$quarter&status=$status&genre=$genre&order=$orderBy",
        navOptions { launchSingleTop = true })
}