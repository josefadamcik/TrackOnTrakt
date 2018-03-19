package cz.josefadamcik.trackontrakt.welcome

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.base.BaseActivity
import cz.josefadamcik.trackontrakt.home.HomeActivity
import cz.josefadamcik.trackontrakt.traktauth.TraktAuthActivity
import javax.inject.Inject

/**
  */
class WelcomeActivity: BaseActivity<WelcomeView, WelcomePresenter>(), WelcomeView {
    @Inject
    lateinit var welcomePresenter: WelcomePresenter
    @BindView(R.id.welcomeText) lateinit var welcomeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as TrackOnTraktApplication).component.inject(this)
        setContentView(R.layout.activity_welcome)
        unbinder = ButterKnife.bind(this)

        welcomeText.movementMethod = LinkMovementMethod.getInstance()
        super.onCreate(savedInstanceState)
    }

    override fun createPresenter(): WelcomePresenter {
        return welcomePresenter
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
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