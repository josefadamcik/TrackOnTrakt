package cz.josefadamcik.trackontrakt.traktauth

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import timber.log.Timber
import javax.inject.Inject

class TraktAuthActivity : AppCompatActivity() {

    @Inject lateinit var authorizationProvider: AuthorizationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trakt_auth)

        (application as TrackOnTraktApplication).graph.inject(this)

        startTraktAuth();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun startTraktAuth() {
        val oauthUrl = authorizationProvider.getOauthAuthorizationUrl()

        val webview = findViewById(R.id.webview) as WebView
        webview.settings.javaScriptEnabled = true
        webview.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Timber.d("webview loaded page %s", url);
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Timber.d("shouldOverrideUrlLoading %s", url)
                if (url != null && url.startsWith(BuildConfig.TRAKT_OAUTH_REDIRECT_URL)) {
                    authorizationProvider.onTraktAuthRedirect(url)
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url)
            }
        })

        webview.loadUrl(oauthUrl)


    }


}
