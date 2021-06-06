package com.sivert.schat2.models

data class UserModel (
    val id:String = "",
    var username:String = "",
    var bio:String = "",
    var fullname:String = "",
    var status:String = "",
    var phone:String = "",
    var photoUrl:String = "empty"
)