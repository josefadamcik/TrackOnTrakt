package cz.josefadamcik.trackontrakt

import cz.josefadamcik.trackontrakt.data.api.ApiRxSchedulers
import io.reactivex.schedulers.Schedulers


public fun givenTestApiScheduler() =
    ApiRxSchedulers(Schedulers.trampoline(), Schedulers.trampoline())
