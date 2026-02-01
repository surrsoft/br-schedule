package com.example.brschedule

enum class ScheduleType(val label: String, val colorRes: Int, val iconRes: Int, val timeRange: String?) {
    DAY("День", R.color.day_shift, R.drawable.ic_sun, "8:00-20:00"),
    NIGHT("Ночь", R.color.night_shift, R.drawable.ic_moon, "20:00-8:00"),
    OFF_TRANSIT("Выходной", R.color.off_shift_1, R.drawable.ic_weekend, null),
    OFF_FULL("Выходной", R.color.off_shift_2, R.drawable.ic_weekend, null);

    companion object {
        fun fromIndex(index: Int): ScheduleType {
            val normalizedIndex = ((index % 4) + 4) % 4
            return values()[normalizedIndex]
        }
    }
}
