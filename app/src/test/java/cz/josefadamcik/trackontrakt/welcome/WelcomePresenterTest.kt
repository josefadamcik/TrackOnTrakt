package cz.josefadamcik.trackontrakt.welcome

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import cz.josefadamcik.trackontrakt.givenTokenHolderWithValidToken
import cz.josefadamcik.trackontrakt.givenTokenHolderWithoutToken
import org.junit.Test

/**
 */
class WelcomePresenterTest {
    private val view = mock<WelcomeView>()

    @Test
    fun attachViewLoggedIn() {
        //given
        val tokenHolder = givenTokenHolderWithValidToken()
        val presenter = WelcomePresenter(tokenHolder)

        //when
        presenter.attachView(view)
        presenter.start()

        //than
        verify(view).navigateToHome()
    }

    @Test
    fun clickOnButtonNavigates() {
        //given
        val tokenHolder = givenTokenHolderWithoutToken()
        val presenter = WelcomePresenter(tokenHolder)

        //when
        presenter.attachView(view)
        presenter.start()
        presenter.onLoginButtonClick()

        //than
        verify(view).navigateToLogin()
    }
}