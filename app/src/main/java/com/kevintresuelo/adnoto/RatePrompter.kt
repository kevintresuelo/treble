/**
 *
 *  Treble Check - Treble Compatibility Checking App
 *  Copyright (C) 2017-2020  KevinT.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see https://www.gnu.org/licenses/gpl-3.0.html.
 *
 */

package com.kevintresuelo.adnoto

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kevintresuelo.treble.R

class RatePrompter(context: Context?, openThreshold: Int = 5) {

    /**
     * Initializes the RatePrompter AlertDialog with context and openThreshold, shows
     * the rate dialog if:
     * 1. it hasn't been dismissed yet; and
     * 2. the app has been opened n times as defined by [openThreshold].
     *
     * However, clicking the neutral button will result to resetting the
     * counter which keeps track of how many times the app has been opened.
     *
     * @param context activity or application context
     * @param openThreshold minimum times that the app should be opened
     *      before the dialog is shown
     */
    init {
        
        val prefsFileKey = "prompt_rate"
        val hasDismissedKey = "rate_dialog_dismissed"
        val timesAppOpenedKey = "rate_dialog_times_app_opened"

        val sharedPreferences = context?.getSharedPreferences(prefsFileKey, Context.MODE_PRIVATE)
        sharedPreferences?.let { sharedPrefs ->

            val isRateDialogAlreadyDismissed = sharedPrefs.getBoolean(hasDismissedKey, false)
            if (!isRateDialogAlreadyDismissed) {

                val timesAppOpened = sharedPrefs.getInt(timesAppOpenedKey, 0) + 1 // timesAppOpened + 1 more for this instance
                if (timesAppOpened >= openThreshold) {

                    MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.rate_title)
                        .setMessage(R.string.rate_message)
                        .setPositiveButton(R.string.rate_action_positive) { dialog, _ ->
                            openStoreListing(context)
                            dialog.dismiss()
                            with (sharedPrefs.edit()) {
                                putBoolean(hasDismissedKey, true)
                                apply()
                            }
                        }
                        .setNeutralButton(R.string.rate_action_neutral) { dialog, _ ->
                            dialog.dismiss()
                            with (sharedPrefs.edit()) {
                                putInt(timesAppOpenedKey, 0)
                                apply()
                            }
                        }
                        .setNegativeButton(R.string.rate_action_negative) { dialog, _ ->
                            dialog.dismiss()
                            with (sharedPrefs.edit()) {
                                putBoolean(hasDismissedKey, true)
                                apply()
                            }
                        }
                        .show()

                } else {

                    with (sharedPrefs.edit()) {
                        putInt(timesAppOpenedKey, timesAppOpened)
                        apply()
                    }

                }

            }

        }

    }

    /**
     * Opens the store listing of this app, falls back to the browser based
     * page if market app is not found.
     */
    private fun openStoreListing(context: Context) {
        val appPackageName = context.packageName
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (anfe: android.content.ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            val packageManager = context.packageManager
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, context.resources.getString(R.string.rate_error_cant_open_url), Toast.LENGTH_SHORT).show()
            }
        }
    }

}