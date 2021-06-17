package com.bb.boombim.data

import android.os.Parcel
import android.os.Parcelable

data class LikeLocation(
        var settingName: String?,
        val name: String?,      // 장소명
        val road: String?,      // 도로명 주소
        val address: String?,   // 지번 주소
        val x: Double,         // 경도(Longitude)
        val y: Double         // 위도(Latitude)
) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readDouble(),
                parcel.readDouble()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(settingName)
                parcel.writeString(name)
                parcel.writeString(road)
                parcel.writeString(address)
                parcel.writeDouble(x)
                parcel.writeDouble(y)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<LikeLocation> {
                override fun createFromParcel(parcel: Parcel): LikeLocation {
                        return LikeLocation(parcel)
                }

                override fun newArray(size: Int): Array<LikeLocation?> {
                        return arrayOfNulls(size)
                }
        }
}
