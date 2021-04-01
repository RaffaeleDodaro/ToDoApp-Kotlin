package com.projectorganizer.projectorganizer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

data class Board(
    val name:String="",
    val image:String="",
    val createdBy:String="",
    val assignedTo:ArrayList<String> = ArrayList()
):
        Parcelable{
            constructor(parcel: Parcel):this(
                parcel.readString()!!, //!! indicano che non deve essere null
                        parcel.readString()!!, //!! indicano che non deve essere null
                        parcel.readString()!!, //!! indicano che non deve essere null
                parcel.createStringArrayList()!! //!! indicano che non deve essere null
            )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(createdBy)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Board> {
        override fun createFromParcel(parcel: Parcel): Board {
            return Board(parcel)
        }

        override fun newArray(size: Int): Array<Board?> {
            return arrayOfNulls(size)
        }
    }
}