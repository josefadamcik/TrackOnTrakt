package cz.josefadamcik.trackontrakt.traktauth

import com.nhaarman.mockito_kotlin.*
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktApiConfig
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import cz.josefadamcik.trackontrakt.data.api.model.OauthTokenRequest
import cz.josefadamcik.trackontrakt.data.api.model.OauthTokenResponse
import cz.josefadamcik.trackontrakt.givenTestApiScheduler
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import junit.framework.Assert.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test
import retrofit2.Response

class AuthorizationProviderTest {

    @Test
    fun getOauthAuthorizationUrl() {
        //given
        val traktApi = mock<TraktApi> {  }
        val traktApiConfig = givenTraktApiConfig()
        val traktAuthTokenHolder = mock<TraktAuthTokenHolder> {}
        val authorizationProvider = AuthorizationProvider(traktApi, traktApiConfig, traktAuthTokenHolder, givenTestApiScheduler())

        //when
        val url = authorizationProvider.getOauthAuthorizationUrl()

        //than
        assertNotNull("Provider returns authorization url", url)
    }


    @Test
    fun shouldHandleRedirectUrl() {
        //given
        val traktApi = mock<TraktApi> {  }
        val traktApiConfig = givenTraktApiConfig()
        val traktAuthTokenHolder = mock<TraktAuthTokenHolder> {}
        val authorizationProvider = AuthorizationProvider(traktApi, traktApiConfig, traktAuthTokenHolder, givenTestApiScheduler())

        //when
        val shouldHandle = authorizationProvider.shouldHandleRedirectUrl(traktApiConfig.oauthRedirectUrl)
        val shouldntHandle = authorizationProvider.shouldHandleRedirectUrl("http://something.else/tralala")

        //than
        assertTrue("redirect url should be handled by app", shouldHandle)
        assertFalse("other url should not be handled by app", shouldntHandle)
    }


    @Test
    fun onTraktAuthRedirect() {
        //given
        val testSubscriber = TestObserver<TraktAuthorizationResult>()
        val authCode = "123"
        val traktApi = mock<TraktApi> {
            on { oauthToken(any()) } doReturn Single.just(
                Response.success(
                    OauthTokenResponse("token", "type", 30000, "refresh_token", "scope", System.currentTimeMillis())
                )
            )
        }
        val traktApiConfig = givenTraktApiConfig()
        val traktAuthTokenHolder = mock<TraktAuthTokenHolder> {}
        val authorizationProvider = AuthorizationProvider(traktApi, traktApiConfig, traktAuthTokenHolder, givenTestApiScheduler())

        //when
        authorizationProvider.requestAuthToken(authCode)
            .subscribe(testSubscriber)

        //than
        argumentCaptor<OauthTokenRequest>().apply {
            verify(traktApi).oauthToken(capture())
            assertThat("code from url was included in OAuthRequest", lastValue.code, equalTo(authCode))
        }
        testSubscriber.assertNoErrors()
        testSubscriber.assertComplete()
        val authResult = testSubscriber.values().first()
        assertNotNull(authResult)
        assertTrue("result is successful", authResult.success)
        assertThat("result has token", authResult.token, not(isEmptyOrNullString()))
    }

    private fun givenTraktApiConfig() =
        TraktApiConfig(
            "clientId",
            "clientsecret",
            "http://example.org/redirect",
            "http://example.org/api",
            "http://example.org/login"
            )


}