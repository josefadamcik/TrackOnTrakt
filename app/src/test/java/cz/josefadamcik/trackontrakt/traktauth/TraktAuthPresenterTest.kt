package cz.josefadamcik.trackontrakt.traktauth

import com.nhaarman.mockito_kotlin.*
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
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
        val presenter = givenPresenter(authProvider, tokenHolder)

        //when
        presenter.attachView(view)

        //than
        verify(view).showProgress()
        verify(view).requestLoginToTraktInBrowser(any())
    }


    @Test
    fun attachViewLoggedIn() {
        //given
        val tokenHolder = givenTokenHolderWithValidToken()
        val authProvider = givenAuthProvider()
        val presenter = TraktAuthPresenter(authProvider, tokenHolder)

        //when
        presenter.attachView(view)

        //than
        verify(view).continueNavigation()
    }

    @Test
    fun onBrowserRedirected() {
        //given
        val tokenHolder = givenTokenHolderWithoutToken()
        val authProvider = givenAuthProvider()
        val presenter = givenPresenter(authProvider, tokenHolder)
        val url = "url"

        //when
        presenter.attachView(view)
        val overriden = presenter.onBrowserRedirected(url)

        //than
        assertTrue("browser loading overriden", overriden)
        verify(view).showProgress()
        verify(view).requestLoginToTraktInBrowser(any())
        verify(authProvider).onTraktAuthRedirect(url)
        verify(view).continueNavigation()
    }

    @Test
    fun onBrowserRedirectedButAuthFailed() {
        //given
        val tokenHolder = givenTokenHolderWithoutToken()
        val authProvider = givenFailingAuthProvider()
        val presenter = givenPresenter(authProvider, tokenHolder)
        val url = "url"

        //when
        presenter.attachView(view)
        val overriden = presenter.onBrowserRedirected(url)

        //than
        assertTrue("browser loading overriden", overriden)
        verify(view).showProgress()
        verify(view).requestLoginToTraktInBrowser(any())
        verify(authProvider).onTraktAuthRedirect(url)
        verify(view).showErrorView()
        verify(view).showErrorMessageWithRetry(any())
    }


    @Test
    fun retry() {
        //given
        val tokenHolder = givenTokenHolderWithValidToken()
        val authProvider = givenAuthProvider()
        val presenter = TraktAuthPresenter(authProvider, tokenHolder)

        //when
        presenter.attachView(view)
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

    private fun givenAuthProvider(redirectResult: Single<TraktAuthorisationResult> = Single.just(TraktAuthorisationResult(true, "dummytoken"))) = mock<AuthorizationProvider> {
        on { getOauthAuthorizationUrl() } doReturn "http://example.com"
        on { shouldHandleRedirectUrl(any()) } doReturn true
        on { onTraktAuthRedirect(any()) } doReturn redirectResult
    }

    private fun givenFailingAuthProvider() = givenAuthProvider(Single.just(TraktAuthorisationResult(false, null)))


    private fun givenPresenter(authProvider: AuthorizationProvider, tokenHolder: TraktAuthTokenHolder) =
        TraktAuthPresenter(authProvider, tokenHolder)


    private fun givenTokenHolderWithoutToken(): TraktAuthTokenHolder = mock<TraktAuthTokenHolder> {
        on { hasToken() } doReturn false
    }
}