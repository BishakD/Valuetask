package com.valuetask.data

import java.util.UUID

/**
 * Represents a single to-do item at every stage of its lifecycle:
 *  - [isPending] = true  → just submitted, API call in flight (spinner shown)
 *  - [isFailed]  = true  → API call failed; item sits in "Uncategorized" with a Retry button
 *  - normal              → categorized successfully
 */
data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val category: String = PENDING_CATEGORY,
    val isPending: Boolean = true,
    val isFailed: Boolean = false,
) {
    companion object {
        /** Internal sentinel — items in this state show a loading spinner. */
        const val PENDING_CATEGORY = "__pending__"

        /** Shown when categorization fails. */
        const val FAILED_CATEGORY = "Uncategorized"

        /**
         * The 12 valid categories returned by the backend, in display order.
         * Items whose category is not in this list are sorted last.
         */
        val KNOWN_CATEGORIES = listOf(
            "Market", "Chores", "Work", "Personal", "Health",
            "Finance", "Education", "Habits", "Family", "Errands",
            "Travel", "Social",
        )

        /** Emoji badge shown next to each category header. */
        fun emoji(category: String) = when (category) {
            "Market"       -> "🛒"
            "Chores"       -> "🧹"
            "Work"         -> "💼"
            "Personal"     -> "✨"
            "Health"       -> "❤️"
            "Finance"      -> "💰"
            "Education"    -> "📚"
            "Habits"       -> "🔁"
            "Family"       -> "👨‍👩‍👧"
            "Errands"      -> "📍"
            "Travel"       -> "✈️"
            "Social"       -> "👥"
            FAILED_CATEGORY -> "❓"
            else           -> "📝"
        }
    }
}
