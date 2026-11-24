package com.example.temu.navigation

object Screen {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val FORM_TEMUAN = "form_temuan"
    const val DETAIL_TEMUAN = "detail_temuan/{id}"
    const val EDIT_TEMUAN = "edit_temuan/{id}"
    const val FORM_KLAIM = "form_klaim/{id}"
    const val KLAIM_SAYA = "klaim_saya"
    const val DETAIL_KLAIM = "detail_klaim/{id}"

    fun detailTemuan(id: String) =
        "detail_temuan/$id"

    fun editTemuan(id: String) =
        "edit_temuan/$id"

    fun formKlaim(id: String) =
        "form_klaim/$id"

    fun detailKlaim(id: String) =
        "detail_klaim/$id"
}