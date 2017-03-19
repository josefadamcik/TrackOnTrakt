package cz.josefadamcik.trackontrakt.traktauth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.home.HomeActivity
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

/**
 * TODO: introduce presenter
 * todo: progress, error view (fullscreen)
 * todo: check if already authorized on start
 * todo: refresh token
 */
class TraktAuthActivity : AppCompatActivity() {

    @Inject lateinit var authorizationProvider: AuthorizationProvider
    val disposable = CompositeDisposable()
    lateinit var webview: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trakt_auth)

        supportActionBar?.setTitle(R.string.title_authorize)

        (application as TrackOnTraktApplication).graph.inject(this)

        webview = findViewById(R.id.webview) as WebView

        startTraktAuth();


    }


    override fun onDestroy() {
        super.onDestroy()

        disposable.clear()
    }



    @SuppressLint("SetJavaScriptEnabled")
    private fun startTraktAuth() {
        val oauthUrl = authorizationProvider.getOauthAuthorizationUrl()


        webview.settings.javaScriptEnabled = true

        webview.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Timber.d("webview loaded page %s", url);
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Timber.d("shouldOverrideUrlLoading %s", url)
                if (url != null && url.startsWith(BuildConfig.TRAKT_OAUTH_REDIRECT_URL)) {
                    onTraktAuthRedirect(url)
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url)
            }
        })

        webview.loadUrl(oauthUrl)


    }

    private fun onTraktAuthRedirect(url: String) {
        disposable.add(
            authorizationProvider.onTraktAuthRedirect(url)
                .subscribe(
                    { res -> continueToHome() },
                    { t ->
                        val snackbar = Snackbar.make(findViewById(R.id.webview), R.string.err_trakt_auth_failed, Snackbar.LENGTH_INDEFINITE)
                        snackbar.setAction(R.string.action_retry, View.OnClickListener { startTraktAuth() })
                        snackbar.show()
                    }
                )
        )
    }

    private fun continueToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
    }


}
