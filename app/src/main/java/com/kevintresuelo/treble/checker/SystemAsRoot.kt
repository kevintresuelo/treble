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

import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException

data class MountPoint(
    val device: String,
    val mountPoint: String,
    val fileSystem: String,
    val prop: String,
    val dummy1: String,
    val dummy2: String
)

object SystemAsRoot {

    fun check(): Boolean? {

        val mountsPoints = ArrayList<MountPoint>()
        val br: BufferedReader

        /**
         * Reads the mounted partitions and checks if the system is mounted as
         * root. Returns null if indeterminable.
         */
        try {
            br = BufferedReader(FileReader("/proc/mounts"))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                line?.let {
                    val mountDetails = it.split(" ").toTypedArray()
                    if (mountDetails.size == 6) {
                        val mountPoint = MountPoint(mountDetails[0], mountDetails[1], mountDetails[2], mountDetails[3], mountDetails[4], mountDetails[5])
                        mountsPoints.add(mountPoint)
                    }
                }
            }

            /**
             * Checks if system has a device block associated with it which isn't temporary
             */
            val systemOnBlock = mountsPoints.none { it.device != "none" && it.mountPoint == "/system" && it.fileSystem != "tmpfs"}

            /**
             * Checks if the device symlink is mounted on root
             */
            val deviceMountedOnRoot = mountsPoints.any { it.device == "/dev/root" && it.mountPoint == "/" }

            /**
             * Checks if a non-temporary block is mounted on system root
             */
            val systemOnRoot = mountsPoints.any { it.mountPoint == "/system_root" && it.fileSystem != "tmpfs" }

            return systemOnBlock || deviceMountedOnRoot || systemOnRoot

        } catch (e: IOException) {
            return null
        } catch (e: FileNotFoundException) {
            return null
        }

    }

}