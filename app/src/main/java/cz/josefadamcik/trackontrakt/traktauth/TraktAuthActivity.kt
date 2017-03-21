package cz.josefadamcik.trackontrakt.traktauth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.hannesdorfmann.mosby3.mvp.MvpActivity
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.home.HomeActivity
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

/**
 * todo: progress, error view (fullscreen)
 * todo: check if already authorized on start
 * todo: refresh token
 */
class TraktAuthActivity : MvpActivity<TraktAuthView, TraktAuthPresenter>(), TraktAuthView {

    @Inject lateinit var traktAuthPresenter: TraktAuthPresenter
    @BindView(R.id.webview) lateinit var webview: WebView

    private var unbinder: Unbinder = Unbinder.EMPTY


    override fun onCreate(savedInstanceState: Bundle?) {
        (application as TrackOnTraktApplication).graph.inject(this)
        setContentView(R.layout.activity_trakt_auth)
        unbinder = ButterKnife.bind(this)

        super.onCreate(savedInstanceState)
        supportActionBar?.setTitle(R.string.title_authorize)

        webview = findViewById(R.id.webview) as WebView

    }

    override fun createPresenter(): TraktAuthPresenter {
        return traktAuthPresenter
    }



    override fun onDestroy() {
        super.onDestroy()
        unbinder.unbind()
    }

    override fun requestLoginToTraktInBrowser(url: String) {
        webview.settings.javaScriptEnabled = true
        webview.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Timber.d("webview loaded page %s", url);
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Timber.d("shouldOverrideUrlLoading %s", url)
                if (url == null || !presenter.onBrowserRedirected(url)) {
                    return super.shouldOverrideUrlLoading(view, url)
                } else {
                    return true
                }
            }
        })
        webview.loadUrl(url)
    }

    override fun continueNavigation() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

    override fun showErrorMessageWithRetry(messageId: Int) {
        val snackbar = Snackbar.make(webview, messageId, Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction(R.string.action_retry, View.OnClickListener { presenter.retry() })
        snackbar.show()
    }


}
