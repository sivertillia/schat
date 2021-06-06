package com.sivert.schat2.utilits

import com.sivert.schat2.database.*

enum class AppStates(val status:String) {
    ONLINE("В сети"),
    OFFLINE("Был недавно"),
    TYPING("Печатает...");

    companion object{
        fun updateState(appStates: AppStates) {
            if (AUTH.currentUser!=null) {
                REF_DATABASE_ROOT.child(
                    NODE_USERS
                ).child(CURRENT_UID).child(
                    CHILD_STATUS
                )
                    .setValue(appStates.status)
                    .addOnSuccessListener { USER.status = appStates.status }
                    .addOnFailureListener { showToast(it.message.toString()) }
            }
        }
    }
}