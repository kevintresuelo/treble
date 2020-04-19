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

import org.w3c.dom.Document
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

data class ABResult(
    val isVirtual: Boolean
)

object AB {

    fun check(): ABResult? {

        /**
         * Checks if the device supports Virtual A/B partition
         */
        if (getProperty("ro.virtual_ab.enabled") == "true" && getProperty("ro.virtual_ab.retrofit") == "false") {
            return ABResult(true)
        }

        /**
         * Checks if the device supports the conventional A/B partition
         */
        if (!getProperty("ro.boot.slot_suffix").isNullOrBlank() || getProperty("ro.build.ab_update") == "true") {
            return ABResult(false)
        }

        /**
         * Returns null if the device doesn't support A/B partitions at all
         */
        return null
    }

}