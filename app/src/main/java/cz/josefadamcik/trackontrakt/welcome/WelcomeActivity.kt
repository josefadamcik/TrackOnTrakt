package cz.josefadamcik.trackontrakt.welcome

import android.content.Intent
import android.os.Bundle
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.hannesdorfmann.mosby3.mvp.MvpActivity
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.home.HomeActivity
import cz.josefadamcik.trackontrakt.traktauth.TraktAuthActivity
import javax.inject.Inject

/**
  */
class WelcomeActivity: MvpActivity<WelcomeView, WelcomePresenter>(), WelcomeView {
    @Inject
    lateinit var welcomePresenter: WelcomePresenter
    private lateinit var unbinder: Unbinder

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as TrackOnTraktApplication).component.inject(this)
        setContentView(R.layout.activity_welcome)
        unbinder = ButterKnife.bind(this)

        super.onCreate(savedInstanceState)
    }

    override fun createPresenter(): WelcomePresenter {
        return welcomePresenter
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbinder.unbind()
    }

    @OnClick(R.id.login_button)
    fun onLoginButtonClick() {
        presenter.onLoginButtonClick()
    }

    override fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

    override fun navigateToLogin() {
        startActivity(Intent(this, TraktAuthActivity::class.java))
    }
}