package cz.josefadamcik.trackontrakt.util

import io.reactivex.Scheduler

/**
 * Enables DI of schedulers and thus simple replacement in tests.
 */
data class RxSchedulers(val subscribe: Scheduler, val observe: Scheduler)