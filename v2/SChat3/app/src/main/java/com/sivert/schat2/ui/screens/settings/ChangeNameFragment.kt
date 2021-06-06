package com.sivert.schat2.ui.screens.settings

import com.sivert.schat2.R
import com.sivert.schat2.database.*
import com.sivert.schat2.ui.screens.base.BaseChangeFragment
import com.sivert.schat2.utilits.*
import kotlinx.android.synthetic.main.fragment_change_name.*


class ChangeNameFragment : BaseChangeFragment(R.layout.fragment_change_name) {

    override fun onResume() {
        super.onResume()
        initFullnameList()
    }

    private fun initFullnameList() {
        val fullnamelist = USER.fullname.split(" ")
        if (fullnamelist.size > 1) {
            settings_input_name.setText(fullnamelist[0])
            settings_input_surname.setText(fullnamelist[1])
        } else settings_input_name.setText(fullnamelist[0])
    }

    override fun change() {
        val name = settings_input_name.text.toString()
        val surname = settings_input_surname.text.toString()

        if (name.isEmpty()) {
            showToast(getString(R.string.settings_toast_name_is_empty))
        } else {
            val fullname = "$name $surname"
            setNameToDatabase(fullname)
        }
    }
}