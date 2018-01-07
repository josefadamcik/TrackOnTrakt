package cz.josefadamcik.trackontrakt.data.api

import io.reactivex.Scheduler

data class ApiRxSchedulers(val subscribe: Scheduler, val observe: Scheduler)