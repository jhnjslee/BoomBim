package com.bb.boombim.data

data class User(
    var email : String?, // 이메일
    val name: String?,      // 이름
    val password: String?, // 비밀번호
    val likeList : ArrayList<LikeLocation>?,      // 좋아요 리스트
)
