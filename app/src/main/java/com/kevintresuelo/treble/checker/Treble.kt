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

package com.kevintresuelo.treble.checker

import android.util.Log
import android.util.Xml
import org.w3c.dom.Document
import org.xmlpull.v1.XmlPullParser
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


data class TrebleResult(
    val isTrebleLegacy: Boolean,
    val isVndkLite: Boolean,
    val vndkVersion: String
)

object Treble {

    fun check(): TrebleResult? {

        val mIsLegacyTreble: Boolean
        val mIsVndkLite: Boolean
        val mVndkVersion: String

        /**
         * Checks for the build.prop if treble is enabled, returns null if the
         * device doesn't include support for Project Treble at all.
         */
        if (getProperty("ro.treble.enabled") != "true") {
            return null
        }

        /**
         * Checks the file location of the vendor manifest to determine if the
         * device uses an old implementation of Project Treble.
         */
        val newVndkManifest = File("/vendor/etc/vintf/manifest.xml")
        val oldVndkManifest = File("/vendor/manifest.xml")
        mIsLegacyTreble = when {
            newVndkManifest.exists() -> false
            oldVndkManifest.exists() -> true
            else -> return null
        }

        /**
         * Checks if the shipped VNDK Lite, which is possibly shipped when the
         * device is launched with Android versions older than Oreo, then just
         * updated to Oreo or newer.
         */
        mIsVndkLite = when (getProperty("ro.vndk.lite")) {
            "true" -> true
            else -> false
        }

        /**
         * Reads the vendor manifest to check for the SELinux Policy version of
         * the device.
         */
        val documentBuilderFactory: DocumentBuilderFactory = DocumentBuilderFactory
            .newInstance()
        val documentBuilder: DocumentBuilder = documentBuilderFactory.newDocumentBuilder()
        val document: Document = documentBuilder.parse(if (mIsLegacyTreble) oldVndkManifest else newVndkManifest)
        mVndkVersion = document.getElementsByTagName("sepolicy").item(0).textContent.trim()

        /**
         * Arranges the result in a data class and returns it
         */
        return TrebleResult(mIsLegacyTreble, mIsVndkLite, mVndkVersion)
    }

}