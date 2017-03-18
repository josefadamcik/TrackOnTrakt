package cz.josefadamcik.trackontrakt.traktauth

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.OauthTokenRequest
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

class TraktAuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trakt_auth)

        startTraktAuth();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun startTraktAuth() {
        val oauthUrl = String.format("https://trakt.tv/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s",
            BuildConfig.TRAKT_CLIENT_ID,
            BuildConfig.TRAKT_OAUTH_REDIRECT_URL
        );

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
                    onTraktAuthRedirect(url)
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url)
            }
        })

        webview.loadUrl(oauthUrl)


    }

    private fun onTraktAuthRedirect(url: String) {
        val uri = Uri.parse(url)
        Timber.i("Parsed uri %s", uri);
        val code = uri.getQueryParameter("code")
        if (code != null) {
            Timber.d("auth code: %s", code)
        }


        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.TRAKT_BASE_API_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        val api = retrofit.create(TraktApi::class.java)

        api.oauthToken(
            OauthTokenRequest(
                code,
                BuildConfig.TRAKT_CLIENT_ID,
                BuildConfig.TRAKT_CLIENT_SECRET,
                BuildConfig.TRAKT_OAUTH_REDIRECT_URL)
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { res ->
                    if (res.isSuccessful) {
                        val response = res.body()
                        Timber.d("obtained access_token %s; response %s", response.access_token, response)
                    } else {
                        Timber.w("failed %s : %s", res.code(), res.message())
                    }
                },
                { t -> Timber.e(t, "Failed") }
            )


    }
}
