# TrackOnTrakt
[![Build Status](https://travis-ci.org/josefadamcik/TrackOnTrakt.svg?branch=master)](https://travis-ci.org/josefadamcik/TrackOnTrakt)
[![codecov](https://codecov.io/gh/josefadamcik/TrackOnTrakt/branch/master/graph/badge.svg)](https://codecov.io/gh/josefadamcik/TrackOnTrakt)



![TrackOnTrakt feature graphics](https://github.com/josefadamcik/TrackOnTrakt/raw/master/feature_graphics.png)

TrackOnTrakt is an opensource Android application for tracking your watched movies and show on  [trakt.tv](https://trakt.tv). You need a trakt.tv account in order to use it.



## Motivation

Main motivation is to develop a nontrivial application, where I can experiment with some technologies 
and practices, I'd like to try. The application can also serve as my personal reference project. 


## Setup

Make copy of keys.properties.example file named keys.properties.
 
### Trakt API key

You'll need to [create a trakt application](https://trakt.tv/oauth/applications/new) and copy 
client id and client secret into the file mentioned above.


## Running the tests

Junit tests:

    ./gradlew test

Espresso tests:

    ./gradlew connectedAndroidTest

## Built With

* [Kotlin](https://kotlinlang.org/)
* [RxJava2](https://github.com/ReactiveX/RxJava), [RxAndriod](https://github.com/ReactiveX/RxAndroid), 
[RxKotlin](https://github.com/ReactiveX/RxKotlin)
* [Mosby](http://hannesdorfmann.com/mosby/)
* [Dagger2](https://google.github.io/dagger/)
* [Retrofit](https://square.github.io/retrofit/)
* [OkHttp](https://github.com/square/okhttp)
* [Glide](https://github.com/bumptech/glide)
* [Timber](https://github.com/JakeWharton/timber)
* [Butterknife](https://jakewharton.github.io/butterknife/)
* [Moshi](https://www.moshi.com/)
* [MaterialProgressBar](https://github.com/DreaminginCodeZH/MaterialProgressBar)
* [ThreeTenABP](https://github.com/JakeWharton/ThreeTenABP)
* [PaperParcel](https://grandstaish.github.io/paperparcel/)
* [AndroidState](https://github.com/evernote/android-state)
* [MaterialSearchBar](https://github.com/mancj/MaterialSearchBar)
* [material-dialogs](https://github.com/afollestad/material-dialogs)
* [MaterialDateTimePicker](https://github.com/wdullaer/MaterialDateTimePicker)


<!--
## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.
-->

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the 
[tags on this repository](https://github.com/josefadamcik/TrackOnTrakt/tags). 

## Authors

* **Josef Adamcik** - *Initial work* - [josefadamcik](https://josef-adamcik.cz/)

<!--
See also the list of [contributors](https://github.com/josefadamcik/TrackOnTrakt/contributors) who participated in this project.
-->

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details


