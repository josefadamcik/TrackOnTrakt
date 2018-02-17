package cz.josefadamcik.trackontrakt.traktauth

import com.nhaarman.mockito_kotlin.*
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import cz.josefadamcik.trackontrakt.util.UriQueryParamParser
import io.reactivex.Single
import org.junit.Assert.assertTrue
import org.junit.Test

class TraktAuthPresenterTest {
    private val view = mock<TraktAuthView>()

    @Test
    fun attachViewNotLoggedIn() {
        //given
        val tokenHolder = givenTokenHolderWithoutToken()
        val authProvider = givenAuthProvider()
        val code = "testcode"
        val presenter = givenPresenter(authProvider, tokenHolder, code)

        //when
        presenter.attachView(view)
        presenter.start()

        //than
        verify(view).showProgress()
        verify(view).requestLoginToTraktInBrowser(any())
    }


    @Test
    fun attachViewLoggedIn() {
        //given
        val tokenHolder = givenTokenHolderWithValidToken()
        val authProvider = givenAuthProvider()
        val presenter = TraktAuthPresenter(authProvider, tokenHolder, givenQueryParamParser("code"))

        //when
        presenter.attachView(view)
        presenter.start()

        //than
        verify(view).continueNavigation()
    }

    @Test
    fun onBrowserRedirected() {
        //given
        val tokenHolder = givenTokenHolderWithoutToken()
        val authProvider = givenAuthProvider()
        val code = "testcode"
        val presenter = givenPresenter(authProvider, tokenHolder, code)
        val url = "mysecheme://myurl?code=$code"

        //when
        presenter.attachView(view)
        presenter.start()
        val overriden = presenter.onBrowserRedirected(url)

        //than
        assertTrue("browser loading overriden", overriden)
        verify(view).showProgress()
        verify(view).requestLoginToTraktInBrowser(any())
        verify(authProvider).requestAuthToken(code)
        verify(view).continueNavigation()
    }

    @Test
    fun onBrowserRedirectedButAuthFailed() {
        //given
        val tokenHolder = givenTokenHolderWithoutToken()
        val authProvider = givenFailingAuthProvider()
        val code = "testcode"
        val presenter = givenPresenter(authProvider, tokenHolder, code)
        val url = "mysecheme://myurl?code=$code"

        //when
        presenter.attachView(view)
        presenter.start()
        val overriden = presenter.onBrowserRedirected(url)

        //than
        assertTrue("browser loading overriden", overriden)
        verify(view).showProgress()
        verify(view).requestLoginToTraktInBrowser(any())
        verify(authProvider).requestAuthToken(code)
        verify(view).showErrorView()
        verify(view).showErrorMessageWithRetry(any())
    }


    @Test
    fun retry() {
        //given
        val tokenHolder = givenTokenHolderWithValidToken()
        val authProvider = givenAuthProvider()
        val presenter = TraktAuthPresenter(authProvider, tokenHolder, givenQueryParamParser("code"))

        //when
        presenter.attachView(view)
        presenter.start()
        presenter.retry()

        //than
        verify(view, times(2)).continueNavigation()
    }

    private fun givenTokenHolderWithValidToken(): TraktAuthTokenHolder {
        return mock<TraktAuthTokenHolder> {
            on { hasToken() } doReturn true
            on { expiresSoonerThanDays(any()) } doReturn false
        }
    }

    private fun givenAuthProvider(redirectResult: Single<TraktAuthorizationResult> = Single.just(TraktAuthorizationResult(true, "dummytoken"))) = mock<AuthorizationProvider> {
        on { getOauthAuthorizationUrl() } doReturn "http://example.com"
        on { shouldHandleRedirectUrl(any()) } doReturn true
        on { requestAuthToken(any()) } doReturn redirectResult
    }

    private fun givenFailingAuthProvider() = givenAuthProvider(Single.just(TraktAuthorizationResult(false, null)))


    private fun givenPresenter(authProvider: AuthorizationProvider, tokenHolder: TraktAuthTokenHolder, code: String) =
        TraktAuthPresenter(authProvider, tokenHolder, givenQueryParamParser(code))

    private fun givenQueryParamParser(codeParamValue: String) = mock<UriQueryParamParser> {
        on { getUriParam(any(), any()) } doReturn codeParamValue
    }

    private fun givenTokenHolderWithoutToken(): TraktAuthTokenHolder = mock<TraktAuthTokenHolder> {
        on { hasToken() } doReturn false
    }
}