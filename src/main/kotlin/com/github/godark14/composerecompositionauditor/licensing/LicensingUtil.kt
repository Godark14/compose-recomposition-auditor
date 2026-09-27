package com.github.godark14.composerecompositionauditor.licensing

import com.intellij.ui.LicensingFacade

object LicensingUtil {

    private const val PRODUCT_CODE = "PCOMPOSERECOMPO"

    fun isPremiumUnlocked(): Boolean? {
        val facade = LicensingFacade.getInstance() ?: return null
        val stamp = facade.getConfirmationStamp(PRODUCT_CODE) ?: return false
        return stamp.isNotBlank()
    }
}