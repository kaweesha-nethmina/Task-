package com.example.task

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson

data class Task(
    val id: Int,
    val name: String,
    val description: String? = null,
    val dueTime: Long? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeValue(dueTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }

        fun fromJson(json: String): Task {
            return Gson().fromJson(json, Task::class.java)
        }
    }

    fun toJson(): String {
        return Gson().toJson(this)
    }
}
