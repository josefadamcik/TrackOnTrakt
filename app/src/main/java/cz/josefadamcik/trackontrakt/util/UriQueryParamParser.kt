package cz.josefadamcik.trackontrakt.util

/**
 * Interface that allows separation of platform specific uri parsing
 * (@see AndroidUriQueryParser) from non android and unit-testable code.
 */
interface UriQueryParamParser {
    fun getUriParam(uri: String, name: String) : String?
}
