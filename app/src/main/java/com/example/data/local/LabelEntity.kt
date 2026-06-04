package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.Label

@Entity(
    tableName = "labels",
    indices = [Index(value = ["name"], unique = true)]
)
data class LabelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
) {
    fun toDomain(): Label {
        return Label(id = id, name = name)
    }

    companion object {
        fun fromDomain(label: Label): LabelEntity {
            return LabelEntity(id = label.id, name = label.name)
        }
    }
}
