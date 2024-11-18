package com.example.coba2.model

import android.os.Parcel
import android.os.Parcelable
import java.util.Calendar
import java.util.UUID

//model: mendefinisikan struktur data utk item todolist
data class Task(
    //val (values) itu immutable, id dihasilkan secara otomatis scra acal dg UUID
    //var (variable) bersifat mutable
    //Tipe nullable (Calendar?) dan (Long?), yang berarti bisa bernilai null
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String,
    var reminderTime: Calendar?,
    val time: Long? = null,
    var isChecked: Boolean = false //nilai default false
) : Parcelable {
    // Parcelable untuk memungkinkan objek Task dikirim antar Activity atau Fragment
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: UUID.randomUUID().toString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong().let { timeInMillis ->
            if (timeInMillis != -1L) {
                Calendar.getInstance().apply {
                    this.timeInMillis = timeInMillis
                }
            } else {
                null
            }
        },
        // Membaca time sebagai Long? (nullable)
        parcel.readLong(),
        parcel.readByte() != 0.toByte() // Membaca isChecked sebagai Boolean
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeLong(reminderTime?.timeInMillis ?: -1L)
        parcel.writeByte(if (isChecked) 1 else 0) // Tulis isChecked sebagai Byte
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task = Task(parcel)
        override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)
    }
}
