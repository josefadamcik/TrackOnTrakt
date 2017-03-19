package cz.josefadamcik.trackontrakt.home

import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bumptech.glide.Glide
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.data.UserAccountManager
import timber.log.Timber
import javax.inject.Inject


class HomeActivity : AppCompatActivity() {

    @Inject lateinit var preferences: SharedPreferences
    @Inject lateinit var userAccountManager: UserAccountManager

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.toolbar_layout) lateinit var toolbarLayout: CollapsingToolbarLayout
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton
    @BindView(R.id.toolbar_image) lateinit var toolbarImage: ImageView
    @BindView(R.id.textView) lateinit var textView: TextView

    private lateinit var unbinder: Unbinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        unbinder = ButterKnife.bind(this)
        setSupportActionBar(toolbar)

        (application as TrackOnTraktApplication).graph.inject(this)

    }

    override fun onStart() {
        super.onStart()

        loadUserProfile()
        loadUserHistory()
    }

    private fun loadUserHistory() {
        userAccountManager.loadUserHistory()
            .subscribe(
                { history -> history.forEach { Timber.i("%s", it) } },
                { t -> Timber.e(t, "error loading history") }
            )

    }

    private fun loadUserProfile() {
        userAccountManager.obtainUserSettings()
            .subscribe(
                { settings ->
                    Timber.d("%s", settings)
                    textView.text = settings.user.name
                    supportActionBar?.title = settings.user.name
                    toolbarLayout.title = settings.user.name
                    //toolbar.title = settings.user.name
                    settings.account.cover_image?.apply {
                        Glide.with(this@HomeActivity).load(this).into(toolbarImage)
                    }
                },
                { t ->
                    Timber.e(t, "unable to load user profile")
                    val snackbar = Snackbar.make(findViewById(R.id.webview), R.string.err_data_retrieval_failed, Snackbar.LENGTH_INDEFINITE)
                    snackbar.setAction(R.string.action_retry, View.OnClickListener { loadUserProfile() })
                    snackbar.show()
                }

            )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
