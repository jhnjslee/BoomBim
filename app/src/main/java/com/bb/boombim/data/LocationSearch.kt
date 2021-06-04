package com.bb.boombim.data

import android.os.Parcel
import android.os.Parcelable

data class LocationSearch(
    val name: String?,
    val address: String?,
    val road: String?,
    val x: Double,
    val y: Double
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeString(road)
        parcel.writeDouble(x)
        parcel.writeDouble(y)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationSearch> {
        override fun createFromParcel(parcel: Parcel): LocationSearch {
            return LocationSearch(parcel)
        }

        override fun newArray(size: Int): Array<LocationSearch?> {
            return arrayOfNulls(size)
        }
    }
}
