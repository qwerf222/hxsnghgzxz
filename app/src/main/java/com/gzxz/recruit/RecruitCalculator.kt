package com.gzxz.recruit

import kotlin.math.min

object RecruitCalculator {
    fun calculateResults(
        selectedTagNames: Set<String>,
        allTags: List<RecruitTag> = RecruitData.tags,
        operators: List<RecruitOperator> = RecruitData.operators,
        maxCombinationSize: Int = 3,
    ): List<TagComboResult> {
        if (selectedTagNames.isEmpty()) {
            return emptyList()
        }

        val selectedTags = allTags.filter { it.name in selectedTagNames }
        if (selectedTags.isEmpty()) {
            return emptyList()
        }

        val combinations = buildList {
            for (size in 1..min(maxCombinationSize, selectedTags.size)) {
                addAll(selectedTags.combinations(size))
            }
        }

        return combinations.mapNotNull { combo ->
            val comboNames = combo.map { it.name }.toSet()
            val matchedOperators = operators
                .filter { operator -> comboNames.all { it in operator.tags } }
                .sortedWith(compareByDescending<RecruitOperator> { it.rarity }.thenBy { it.name })

            if (matchedOperators.isEmpty()) {
                null
            } else {
                val guaranteedRarity = matchedOperators.minOf { it.rarity }
                val highestRarity = matchedOperators.maxOf { it.rarity }
                val averageRarity = matchedOperators.map { it.rarity }.average()
                TagComboResult(
                    tags = combo,
                    operators = matchedOperators,
                    guaranteedRarity = guaranteedRarity,
                    highestRarity = highestRarity,
                    averageRarity = averageRarity,
                    priorityScore = combo.sumOf { it.priority },
                )
            }
        }.sortedWith(
            compareByDescending<TagComboResult> { it.tags.size }
                .thenByDescending { it.guaranteedRarity }
                .thenByDescending { it.highestRarity }
                .thenByDescending { it.priorityScore }
                .thenBy { it.operators.size }
                .thenByDescending { it.averageRarity }
                .thenBy { it.title },
        )
    }
}

private fun <T> List<T>.combinations(size: Int): List<List<T>> {
    if (size <= 0 || size > this.size) {
        return emptyList()
    }
    if (size == 1) {
        return map { listOf(it) }
    }

    val results = mutableListOf<List<T>>()
    for (index in 0..this.size - size) {
        val head = this[index]
        val tail = subList(index + 1, this.size)
        tail.combinations(size - 1).forEach { combination ->
            results += listOf(head) + combination
        }
    }
    return results
}

