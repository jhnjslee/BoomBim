package com.bb.boombim.data

import android.os.Parcel
import android.os.Parcelable

data class DbData(  val x: Double,
                    val y: Double) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(x)
        parcel.writeDouble(y)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DbData> {
        override fun createFromParcel(parcel: Parcel): DbData {
            return DbData(parcel)
        }

        override fun newArray(size: Int): Array<DbData?> {
            return arrayOfNulls(size)
        }
    }

}
