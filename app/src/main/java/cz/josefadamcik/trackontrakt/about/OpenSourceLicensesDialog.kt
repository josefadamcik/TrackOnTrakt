package cz.josefadamcik.trackontrakt.about

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.webkit.WebView
import cz.josefadamcik.trackontrakt.R


class OpenSourceLicensesDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val webView = WebView(activity)
        webView.loadUrl("file:///android_asset/open_source_licenses.html")

        return AlertDialog.Builder(activity)
                .setTitle(R.string.about_licenses_dialog_title)
                .setView(webView)
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() }
                )
                .create()
    }
}