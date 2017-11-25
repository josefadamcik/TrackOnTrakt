
package cz.josefadamcik.trackontrakt.base

import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<V : MvpView> : MvpPresenter<V> {
    protected val disposables = CompositeDisposable()
    var view: V? = null

    override fun attachView(view: V) {
        this.view = view
    }

    override fun detachView(retainInstance: Boolean) {
        disposables.clear()
    }


}