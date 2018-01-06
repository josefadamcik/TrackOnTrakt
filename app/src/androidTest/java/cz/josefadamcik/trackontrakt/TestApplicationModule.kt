package cz.josefadamcik.trackontrakt

import cz.josefadamcik.trackontrakt.testutil.TestTimeProvider
import cz.josefadamcik.trackontrakt.util.CurrentTimeProvider
import dagger.Module


@Module
class TestApplicationModule(app: TrackOnTraktApplication) : ApplicationModule(app) {
    protected override fun createCurrentTimeProvider(): CurrentTimeProvider = TestTimeProvider
}