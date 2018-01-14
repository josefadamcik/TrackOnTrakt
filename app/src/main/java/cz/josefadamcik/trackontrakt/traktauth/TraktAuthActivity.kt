package cz.josefadamcik.trackontrakt.traktauth

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.hannesdorfmann.mosby3.mvp.MvpActivity
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.home.HomeActivity
import timber.log.Timber
import javax.inject.Inject

/**
 * OAuth authorisation, token refresh.
 */
class TraktAuthActivity : MvpActivity<TraktAuthView, TraktAuthPresenter>(), TraktAuthView {

    @Inject lateinit var traktAuthPresenter: TraktAuthPresenter
    @BindView(R.id.webview) lateinit var webview: WebView
    @BindView(R.id.progress) lateinit var progress: ProgressBar
    @BindView(R.id.errorView) lateinit var errorView: ImageView

    private var unbinder: Unbinder = Unbinder.EMPTY


    override fun onCreate(savedInstanceState: Bundle?) {
        (application as TrackOnTraktApplication).component.inject(this)
        setContentView(R.layout.activity_trakt_auth)
        unbinder = ButterKnife.bind(this)

        super.onCreate(savedInstanceState)
        supportActionBar?.setTitle(R.string.title_authorize)

        webview = findViewById<WebView>(R.id.webview) as WebView

    }

    override fun createPresenter(): TraktAuthPresenter {
        return traktAuthPresenter
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        webview.destroy()
        unbinder.unbind()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun requestLoginToTraktInBrowser(url: String) {
        webview.visibility = VISIBLE
        errorView.visibility = GONE

        webview.settings.javaScriptEnabled = true
        webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                showProgress()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Timber.d("webview loaded page %s", url);
                hideProgress()
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Timber.e("webview error: %s %s", errorCode, description)
                hideProgress()
                showErrorView()
                showErrorMessageWithRetry(R.string.err_unknown)
            }

            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                super.onReceivedHttpError(view, request, errorResponse)
                Timber.e("webview http error: %s", errorResponse)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Timber.d("shouldOverrideUrlLoading %s", url)
                if (url == null || !presenter.onBrowserRedirected(url)) {
                    return super.shouldOverrideUrlLoading(view, url)
                } else {
                    return true
                }
            }
        }
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

    override fun showErrorView() {
        webview.visibility = GONE
        errorView.visibility = VISIBLE
    }

    override fun showProgress() {
        progress.visibility = VISIBLE
    }

    override fun hideProgress() {
        progress.visibility = GONE

    }
}
