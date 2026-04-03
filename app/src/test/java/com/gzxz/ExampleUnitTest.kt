package com.gzxz

import com.gzxz.recruit.RecruitCalculator
import com.gzxz.recruit.RecruitData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun generatedDataExposesExpectedWorkbookMetadata() {
        assertEquals(listOf("职业标签", "种族标签", "属性标签", "地形标签"), RecruitData.tagGroups.map { it.label })
        assertEquals(23, RecruitData.tags.size)
        assertEquals(128, RecruitData.operators.size)
    }

    @Test
    fun threeTagCombinationReturnsExpectedOperator() {
        val results = RecruitCalculator.calculateResults(
            setOf("射手", "神灵", "光系"),
        )

        val exactResult = results.first { it.tags.map { tag -> tag.name }.toSet() == setOf("射手", "神灵", "光系") }
        assertEquals(listOf("星灵射手"), exactResult.operators.map { it.name })
        assertEquals(3, exactResult.guaranteedRarity)
    }

    @Test
    fun highRarityThreeTagCombinationIsRankedAheadOfBroaderMatches() {
        val results = RecruitCalculator.calculateResults(
            setOf("射手", "神灵", "光系", "平原"),
        )

        assertEquals(3, results.first().tags.size)
        assertEquals(setOf("射手", "神灵", "光系"), results.first().tags.map { it.name }.toSet())
        assertEquals(listOf("星灵射手"), results.first().operators.map { it.name })
    }

    @Test
    fun twoTagCombinationKeepsOperatorsSortedByRarity() {
        val results = RecruitCalculator.calculateResults(
            setOf("战士", "亡灵"),
        )

        val exactResult = results.first { it.tags.map { tag -> tag.name }.toSet() == setOf("战士", "亡灵") }
        assertEquals(listOf("幽灵船长", "幽灵大副", "幽灵水手"), exactResult.operators.map { it.name })
        assertTrue(exactResult.operators.zipWithNext().all { (left, right) -> left.rarity >= right.rarity })
    }
}