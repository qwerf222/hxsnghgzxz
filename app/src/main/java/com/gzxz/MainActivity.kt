package com.gzxz

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.gzxz.databinding.ActivityMainBinding
import com.gzxz.recruit.RecruitCalculator
import com.gzxz.recruit.RecruitData
import com.gzxz.recruit.RecruitOperator
import com.gzxz.recruit.RecruitTag
import com.gzxz.recruit.TagGroup
import com.gzxz.recruit.TagComboResult
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val selectedTags = linkedSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        binding.resetButton.setOnClickListener {
            selectedTags.clear()
            renderAll()
        }

        renderAll()
    }

    private fun renderAll() {
        renderTagGroups()
        renderSelectedTags()
        renderResults()
    }

    private fun renderTagGroups() {
        binding.tagSectionContainer.removeAllViews()
        val tagGroups = RecruitData.tagGroups.sortedBy { it.order }
        tagGroups.forEachIndexed { index, group ->
            val tags = RecruitData.tags.filter { it.groupKey == group.key }
            binding.tagSectionContainer.addView(
                createTagSection(
                    group = group,
                    tags = tags,
                    addTopMargin = index > 0,
                ),
            )
        }
    }

    private fun renderCategoryChips(chipGroup: ChipGroup, tags: List<RecruitTag>) {
        chipGroup.removeAllViews()
        tags.forEach { tag ->
            chipGroup.addView(createSelectableTagChip(tag))
        }
    }

    private fun createTagSection(group: TagGroup, tags: List<RecruitTag>, addTopMargin: Boolean): View {
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                if (addTopMargin) {
                    topMargin = dp(14)
                }
            }
        }

        val titleView = TextView(this).apply {
            text = group.label
            setTextColor(color(R.color.text_primary))
            setTypeface(typeface, Typeface.BOLD)
            textSize = 14f
        }

        val chipGroup = ChipGroup(this).apply {
            isSingleLine = false
            chipSpacingHorizontal = dp(8)
            chipSpacingVertical = dp(8)
            setPadding(0, dp(12), 0, 0)
        }

        renderCategoryChips(chipGroup, tags)
        section.addView(titleView)
        section.addView(chipGroup)
        return section
    }

    private fun renderSelectedTags() {
        binding.selectedChipGroup.removeAllViews()

        if (selectedTags.isEmpty()) {
            val chip = Chip(this).apply {
                text = getString(R.string.selected_none)
                isClickable = false
                chipBackgroundColor = ColorStateList.valueOf(color(R.color.surface_card))
                chipStrokeWidth = dp(1).toFloat()
                chipStrokeColor = ColorStateList.valueOf(color(R.color.border_soft))
                setTextColor(color(R.color.text_secondary))
            }
            binding.selectedChipGroup.addView(chip)
            return
        }

        selectedTags.forEach { tagName ->
            val chip = Chip(this).apply {
                text = tagName
                isCloseIconVisible = true
                closeIconTint = ColorStateList.valueOf(color(R.color.primary_500))
                chipBackgroundColor = ColorStateList.valueOf(color(R.color.chip_selected_bg))
                chipStrokeWidth = dp(1).toFloat()
                chipStrokeColor = ColorStateList.valueOf(color(R.color.primary_500))
                setTextColor(color(R.color.chip_selected_text))
                setOnCloseIconClickListener {
                    selectedTags.remove(tagName)
                    renderAll()
                }
                setOnClickListener {
                    selectedTags.remove(tagName)
                    renderAll()
                }
            }
            binding.selectedChipGroup.addView(chip)
        }
    }

    private fun renderResults() {
        binding.resultContainer.removeAllViews()
        binding.resultSummaryText.text = getString(R.string.result_section_title)

        if (selectedTags.size < MIN_RESULT_TAG_COUNT) {
            showEmptyState(
                title = getString(R.string.empty_state_title),
                message = getString(R.string.empty_state_message),
                summary = getString(R.string.result_summary_pick_three),
            )
            return
        }

        val results = RecruitCalculator.calculateResults(selectedTags)
            .filter { it.tags.size == MIN_RESULT_TAG_COUNT }
            .take(MAX_RESULT_GROUPS)

        if (results.isEmpty()) {
            showEmptyState(
                title = getString(R.string.no_match_title),
                message = getString(R.string.no_match_message),
                summary = getString(R.string.result_summary_no_match),
            )
            return
        }

        binding.emptyStateCard.isVisible = false
        binding.resultSummaryText.text = getString(R.string.result_summary_count, results.size)
        results.forEach { result ->
            binding.resultContainer.addView(createResultCard(result))
        }
    }

    private fun showEmptyState(title: String, message: String, summary: String) {
        binding.emptyStateCard.isVisible = true
        binding.emptyStateTitle.text = title
        binding.emptyStateMessage.text = message
        binding.resultSummaryText.text = summary
    }

    private fun createSelectableTagChip(tag: RecruitTag): Chip {
        val checked = tag.name in selectedTags
        return Chip(this).apply {
            text = tag.name
            isCheckable = true
            isCheckedIconVisible = false
            isChecked = checked
            chipBackgroundColor = ColorStateList.valueOf(
                if (checked) color(R.color.chip_selected_bg) else color(R.color.surface_card),
            )
            chipStrokeWidth = dp(1).toFloat()
            chipStrokeColor = ColorStateList.valueOf(
                if (checked) color(R.color.primary_500) else color(R.color.border_soft),
            )
            setTextColor(
                if (checked) color(R.color.chip_selected_text) else color(R.color.text_primary),
            )
            setOnClickListener {
                toggleTag(tag.name)
            }
        }
    }

    private fun toggleTag(tagName: String) {
        if (selectedTags.contains(tagName)) {
            selectedTags.remove(tagName)
            renderAll()
            return
        }

        if (selectedTags.size >= MAX_SELECTED_TAGS) {
            Toast.makeText(this, getString(R.string.selection_limit_message), Toast.LENGTH_SHORT).show()
            return
        }

        selectedTags.add(tagName)
        renderAll()
    }

    private fun createResultCard(result: TagComboResult): View {
        val card = MaterialCardView(this).apply {
            radius = dp(24).toFloat()
            cardElevation = 0f
            strokeWidth = dp(1)
            strokeColor = color(R.color.border_soft)
            setCardBackgroundColor(color(R.color.surface_card))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                bottomMargin = dp(12)
            }
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }

        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleView = TextView(this).apply {
            text = result.title
            setTextColor(color(R.color.text_primary))
            setTypeface(typeface, Typeface.BOLD)
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val countBadge = TextView(this).apply {
            text = getString(R.string.combo_count_badge, result.operators.size)
            setTextColor(color(R.color.primary_700))
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            background = createRoundedBackground(
                fillColor = ColorUtils.setAlphaComponent(color(R.color.primary_500), 24),
                strokeColor = ColorUtils.setAlphaComponent(color(R.color.primary_500), 80),
            )
            setPadding(dp(12), dp(6), dp(12), dp(6))
        }

        val qualityView = TextView(this).apply {
            text = getString(R.string.combo_quality, result.guaranteedRarity)
            setTextColor(color(rarityColor(result.highestRarity)))
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(10), 0, 0)
        }

        val sortHintView = TextView(this).apply {
            text = buildOperatorSummary(result)
            setTextColor(color(R.color.text_secondary))
            textSize = 13f
            setPadding(0, dp(6), 0, 0)
        }

        val operatorGroup = ChipGroup(this).apply {
            isSingleLine = false
            chipSpacingHorizontal = dp(8)
            chipSpacingVertical = dp(8)
            setPadding(0, dp(14), 0, 0)
        }

        result.operators.forEach { operator ->
            operatorGroup.addView(createOperatorChip(operator))
        }

        titleRow.addView(titleView)
        titleRow.addView(countBadge)
        content.addView(titleRow)
        content.addView(qualityView)
        content.addView(sortHintView)
        content.addView(operatorGroup)
        card.addView(content)
        return card
    }

    private fun createOperatorChip(operator: RecruitOperator): Chip {
        val rarityColor = color(rarityColor(operator.rarity))
        return Chip(this).apply {
            text = getString(R.string.operator_chip_label, operator.name, operator.rarity)
            isClickable = false
            chipBackgroundColor = ColorStateList.valueOf(ColorUtils.setAlphaComponent(rarityColor, 26))
            chipStrokeColor = ColorStateList.valueOf(ColorUtils.setAlphaComponent(rarityColor, 96))
            chipStrokeWidth = dp(1).toFloat()
            setTextColor(rarityColor)
        }
    }

    private fun buildOperatorSummary(result: TagComboResult): String {
        val topNames = result.operators.take(3).joinToString("、") { it.name }
        return if (result.operators.size <= 3) {
            getString(R.string.operator_summary_exact, topNames)
        } else {
            getString(R.string.operator_summary_more, topNames, result.operators.size)
        }
    }

    private fun rarityColor(rarity: Int): Int = when (rarity) {
        6 -> R.color.rarity_6
        5 -> R.color.rarity_5
        4 -> R.color.rarity_4
        3 -> R.color.rarity_3
        2 -> R.color.rarity_2
        else -> R.color.rarity_1
    }

    private fun createRoundedBackground(fillColor: Int, strokeColor: Int) =
        android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = dp(999).toFloat()
            setColor(fillColor)
            setStroke(dp(1), strokeColor)
        }

    private fun color(resId: Int): Int = ContextCompat.getColor(this, resId)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    private companion object {
        const val MAX_SELECTED_TAGS = 5
        const val MIN_RESULT_TAG_COUNT = 3
        const val MAX_RESULT_GROUPS = 3
    }
}

