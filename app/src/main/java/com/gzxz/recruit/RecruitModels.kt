package com.gzxz.recruit

data class TagGroup(
    val key: String,
    val label: String,
    val order: Int,
)

data class RecruitTag(
    val name: String,
    val groupKey: String,
    val priority: Int,
)

data class RecruitOperator(
    val name: String,
    val rarity: Int,
    val tags: Set<String>,
)

data class TagComboResult(
    val tags: List<RecruitTag>,
    val operators: List<RecruitOperator>,
    val guaranteedRarity: Int,
    val highestRarity: Int,
    val averageRarity: Double,
    val priorityScore: Int,
) {
    val title: String = tags.joinToString(" · ") { it.name }
}

