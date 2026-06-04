package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.model.ChecklistItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @TypeConverter
    fun fromChecklistItems(value: List<ChecklistItem>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, ChecklistItem::class.java)
        val adapter = moshi.adapter<List<ChecklistItem>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toChecklistItems(value: String?): List<ChecklistItem>? {
        if (value.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, ChecklistItem::class.java)
        val adapter = moshi.adapter<List<ChecklistItem>>(type)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun fromLabelIds(value: List<Long>?): String? {
        if (value == null) return null
        return value.joinToString(",")
    }

    @TypeConverter
    fun toLabelIds(value: String?): List<Long>? {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",").mapNotNull { it.toLongOrNull() }
    }
}
