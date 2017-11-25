
package cz.josefadamcik.trackontrakt


import cz.josefadamcik.trackontrakt.data.api.TestApiModule
import dagger.Component


@ApplicationScope
@Component(modules = arrayOf(ApplicationModule::class, TestApiModule::class))
interface TestApplicationComponent : ApplicationComponent {
}