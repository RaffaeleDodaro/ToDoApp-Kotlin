package com.projectorganizer.projectorganizer.models

import android.os.Parcel
import android.os.Parcelable

data class User (
    val id:String="",
    val name:String ="",
    val email:String="",
    val image:String="",
    val fcmToken:String="" //token associato ad ogni utente
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun describeContents()=0

    override fun writeToParcel(dest: Parcel, flags: Int) =with(dest) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(image)
        writeString(fcmToken)
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}