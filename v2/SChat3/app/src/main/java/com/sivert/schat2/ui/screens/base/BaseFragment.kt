package com.sivert.schat2.ui.screens.base

import androidx.fragment.app.Fragment
import com.sivert.schat2.utilits.APP_ACTIVITY


open class BaseFragment(layout: Int) : Fragment(layout) {

    override fun onStart() {
        super.onStart()
        APP_ACTIVITY.mAppDrawer.disableDrawer()
    }
}