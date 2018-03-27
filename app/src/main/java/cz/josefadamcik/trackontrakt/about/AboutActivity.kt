package cz.josefadamcik.trackontrakt.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.R


class AboutActivity : AppCompatActivity() {
    @BindView(R.id.info) lateinit var infoText: TextView
    @BindView(R.id.versionText) lateinit var versionText: TextView


    companion object {
        fun createIntent(context: Context) = Intent(context, AboutActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        ButterKnife.bind(this)

        supportActionBar?.title = getString(R.string.about)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        infoText.movementMethod = LinkMovementMethod.getInstance()
        versionText.text = getString(R.string.about_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @OnClick(R.id.licenses)
    fun onLicensesClick() {
        val tag = "dialog_licenes"
        val ft = fragmentManager.beginTransaction()
        val prev = fragmentManager.findFragmentByTag(tag)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        OpenSourceLicensesDialog().show(ft, tag)
    }


}
