/*
 Copyright 2017 Josef Adamcik <josef.adamcik@gmail.com>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package cz.josefadamcik.trackontrakt.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import butterknife.Unbinder
import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import com.hannesdorfmann.mosby3.mvp.delegate.ActivityMvpDelegateImpl
import com.hannesdorfmann.mosby3.mvp.delegate.MvpDelegateCallback
import com.lapism.searchview.SearchAdapter
import io.reactivex.disposables.CompositeDisposable


public abstract class BaseActivity<V : MvpView, P : MvpPresenter<V>> : AppCompatActivity(), MvpDelegateCallback<V, P>, MvpView {
    private val mvpDelegate: ActivityMvpDelegateImpl<V, P> = ActivityMvpDelegateImpl<V, P>(this, this, true)
    private lateinit var presenterField: P
    protected var retainInstance: Boolean = false

    private lateinit var suggestionAdapter: SearchAdapter

    protected val disposable: CompositeDisposable = CompositeDisposable()

    protected var unbinder: Unbinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mvpDelegate.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mvpDelegate.onDestroy()
        unbinder?.unbind()
        disposable.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mvpDelegate.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        mvpDelegate.onPause()
    }

    override fun onResume() {
        super.onResume()
        mvpDelegate.onResume()
    }

    override fun onStart() {
        super.onStart()
        mvpDelegate.onStart()
    }

    override fun onStop() {
        super.onStop()
        mvpDelegate.onStop()
    }

    override fun onRestart() {
        super.onRestart()
        mvpDelegate.onRestart()
    }

    override fun onContentChanged() {
        super.onContentChanged()
        mvpDelegate.onContentChanged()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mvpDelegate.onPostCreate(savedInstanceState)
    }

    /**
     * Instantiate a presenter instance

     * @return The [MvpPresenter] for this view
     */
    abstract override fun createPresenter(): P

    override fun setPresenter(presenter: P) {
        this.presenterField = presenter
    }

    override fun getPresenter(): P {
        return presenterField
    }

    override fun getMvpView(): V {
        return this as V
    }
}