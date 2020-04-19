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

package com.kevintresuelo.treble.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.kevintresuelo.treble.R

fun openUrl(activity: Activity, uriString: String, shouldFinish: Boolean, action: String = Intent.ACTION_VIEW) {
    val intent = Intent(action)
    intent.data = Uri.parse(uriString)
    openUrl(activity, intent, shouldFinish)
}

fun openUrl(activity: Activity, intent: Intent, shouldFinish: Boolean = false) {
    val packageManager = activity.packageManager
    if (intent.resolveActivity(packageManager) != null) {
        activity.startActivity(intent)
        if (shouldFinish) {
            activity.finish()
        }
    } else {
        Toast.makeText(activity, activity.resources.getString(R.string.utils_error_cant_open_url), Toast.LENGTH_SHORT).show()
    }
}

fun toOrdinal(i: Int): String {
    val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
    return when (i % 100) {
        11, 12, 13 -> i.toString() + "th"
        else -> i.toString() + suffixes[i % 10]
    }
}