package com.xioneko.android.nekoanime.data.model

enum class AnimeCategory(
    val title: String,
    val options: List<Pair<String, String>>, // <输出值，标签值>
) {
    Type(
        title = "类型",
        options = listOf("1" to "日本动漫", "2" to "动漫电影", "3" to "欧美动漫", "4" to "国产动漫")
    ),
    Order(
        title = "排序",
        options = listOf("time" to "最新", "hits" to "最热", "score" to "评分")
    ),
    Genre(
        title = "风格",
        options = listOf(
            "" to "全部",
            "喜剧" to "喜剧",
            "爱情" to "爱情",
            "恐怖" to "恐怖",
            "动作" to "动作",
            "科幻" to "科幻",
            "剧情" to "剧情",
            "战争" to "战争",
            "犯罪" to "犯罪",
            "奇幻" to "奇幻",
            "冒险" to "冒险",
            "悬疑" to "悬疑",
            "惊悚" to "惊悚",
            "历史" to "历史",
            "运动" to "运动",
            "儿童" to "儿童"
        )
    ),
    Year(
        title = "年份",
        options = buildList {
            add("" to "全部")
            for (year in java.time.LocalDate.now().year downTo 2001) {
                add(year.toString() to year.toString())
            }
            add("2000" to "更早")
        },
    )
}

fun AnimeCategory.defaultLabel() = options.first().second

fun AnimeCategory.labelValueOf(option: String): String =
    options.find { it.first == option }?.second
        ?: throw RuntimeException("Invalid Option: $option")