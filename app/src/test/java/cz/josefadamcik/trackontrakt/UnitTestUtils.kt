package cz.josefadamcik.trackontrakt

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import cz.josefadamcik.trackontrakt.util.RxSchedulers
import io.reactivex.schedulers.Schedulers


public fun givenTestApiScheduler() =
        RxSchedulers(Schedulers.trampoline(), Schedulers.trampoline())


public fun givenTokenHolderWithValidToken(): TraktAuthTokenHolder {
    return mock<TraktAuthTokenHolder> {
        on { hasToken() } doReturn true
        on { expiresSoonerThanDays(any()) } doReturn false
    }
}


public fun givenTokenHolderWithoutToken(): TraktAuthTokenHolder = mock<TraktAuthTokenHolder> {
    on { hasToken() } doReturn false
}