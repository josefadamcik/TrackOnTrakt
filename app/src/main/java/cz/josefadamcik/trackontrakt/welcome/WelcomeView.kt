package cz.josefadamcik.trackontrakt.welcome

import com.hannesdorfmann.mosby3.mvp.MvpView

/**
 */
interface WelcomeView: MvpView {
    fun navigateToHome()
    fun navigateToLogin()
}