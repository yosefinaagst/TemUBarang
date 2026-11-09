
package com.example.temu.navigation

object Screen {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val FORM_TEMUAN = "form_temuan"
    const val DETAIL_TEMUAN = "detail_temuan/{id}"

    fun detailTemuan(id: String): String {
        return "detail_temuan/$id"
    }
}
