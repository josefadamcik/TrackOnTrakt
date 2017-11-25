

package cz.josefadamcik.trackontrakt.data.api

class ApiException(message: String, val responseCode: Int, val responseMessage: String) : Exception(message)
