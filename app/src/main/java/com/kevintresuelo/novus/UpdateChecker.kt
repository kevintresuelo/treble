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

package com.kevintresuelo.novus

import android.app.Activity
import android.content.IntentSender
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import com.kevintresuelo.treble.BuildConfig
import com.kevintresuelo.treble.R

/**
 * To use the update checker:
 * 1. Initialize the UpdateChecker in your app's entry points, and then
 *      call checkForUpdates().
 * 2. On the activity/fragment's onResume(), call
 *      checkForStalledUpdates() to check if there are any stalled
 *      updates on the background.
 * 3. On the activity/fragment's onActivityResult(), call
 *      assessActivityResult() to handle the event results from the
 *      updater.
 * 4. Don't forget to pass `true` on triggeredByUser if the call is made
 *      by the user. Otherwise, flexible updates might not work.
 *
 * @param activity the activity that calls on this object
 * @param rootView the view that the Snackbars can attach to
 * @param triggeredByUser whether the update is triggered by the user, or
 *      initiated by a logic based code.
 */
class UpdateChecker(private val activity: Activity, private val rootView: View, private val triggeredByUser: Boolean) {

    companion object {
        const val UPDATE_REQUEST_CODE = 7845

        fun assessActivityResult(requestCode: Int, resultCode: Int, rootView: View) {
            if (requestCode == UPDATE_REQUEST_CODE) {
                if (resultCode != Activity.RESULT_OK) {
                    Snackbar.make(rootView, R.string.updater_feedback_error, Snackbar.LENGTH_LONG)
                }
            }
        }
    }

    val context = activity
    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(context) }

    private val appUpdatedListener: InstallStateUpdatedListener by lazy {
        object : InstallStateUpdatedListener {
            override fun onStateUpdate(installState: InstallState) {
                when {
                    installState.installStatus() == InstallStatus.DOWNLOADED -> popupSnackbarForCompleteUpdate()
                    installState.installStatus() == InstallStatus.INSTALLED -> appUpdateManager.unregisterListener(this)
                }
            }
        }
    }

    fun checkForUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        checkForUpdates(appUpdateInfoTask, activity)
    }

    fun checkForStalledUpdates() {
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->

                // If the update is downloaded but not installed,
                // notify the user to complete the update.
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate()
                }

                //Check if Immediate update is required
                try {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        // If an in-app update is already running, resume the update.
                        /**
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            activity,
                            UPDATE_REQUEST_CODE)
                         */
                    }
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }
    }

    private fun checkForUpdates(appUpdateInfoTask: Task<AppUpdateInfo>, activity: Activity) {

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

            val newVersionCode = appUpdateInfo.availableVersionCode()
            val oldVersionCode = BuildConfig.VERSION_CODE
            val roundedNewVersionCode = newVersionCode - (newVersionCode % 100000)
            val roundedOldVersionCode = oldVersionCode - (oldVersionCode % 100000)

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                val installType = when {
                    (roundedNewVersionCode - roundedOldVersionCode) >= 100000 || isUpdateAlreadyStale(appUpdateInfo, 30) -> AppUpdateType.IMMEDIATE
                    isUpdateAlreadyStale(appUpdateInfo, 7) || triggeredByUser -> AppUpdateType.FLEXIBLE
                    else -> null
                }

                if (installType == AppUpdateType.FLEXIBLE) appUpdateManager.registerListener(appUpdatedListener)

                if (installType != null) {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, installType, activity, UPDATE_REQUEST_CODE)
                }
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE && triggeredByUser) {
                    Snackbar.make(rootView, R.string.updater_feedback_up_to_date, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun isUpdateAlreadyStale(appUpdateInfo: AppUpdateInfo, daysThreshold: Int): Boolean {
        return (appUpdateInfo.clientVersionStalenessDays() != null && appUpdateInfo.clientVersionStalenessDays() >= daysThreshold)
    }

    private fun popupSnackbarForCompleteUpdate() {
        Snackbar.make(
            rootView,
            R.string.updater_flexible_complete_prompt_message,
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(R.string.updater_flexible_complete_prompt_action_message) { appUpdateManager.completeUpdate() }
            show()
        }
    }


}