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

import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.FileReader

data class ArchitectureResult(
    val cpuArch: ABI
)

enum class ABI {
    ARM32,
    ARM32_BINDER64,
    ARM64,
    X86,
    X86_64
}

object CPUArchitecture {

    fun check(): ArchitectureResult? {

        /**
         * Initializes the software-supported ABI, which is the preferred ABI and
         * is listed first in Build.SUPPORTED_ABIS.
         */
        val supportedABIs = Build.SUPPORTED_ABIS
        val cpuArch = supportedABIs.first()

        /**
         * Determines the bitness through the instructure sets supported by the
         * OS.
         */
        val softwareResult = when {
            cpuArch.contains("arm64-v8a") -> ABI.ARM64
            cpuArch.contains("armeabi-v7a") -> ABI.ARM32
            cpuArch.contains("x86_64") -> ABI.X86_64
            cpuArch.contains("x86") -> ABI.X86
            else -> null
        }

        /**
         * Determines the bitness through the CPU architecture.
         */
        var hardwareResult: String? = null
        try {
            val br = BufferedReader(FileReader("/proc/cpuinfo"))
            var line: String?
            var stop = false
            while (br.readLine().also { line = it } != null && !stop) {
                line?.let {
                    val data = it.split(":")

                    if (data.size > 1) {
                        val key = data[0].trim().replace(" ", "_")
                        if (key.equals("cpu_architecture", true)) {
                            hardwareResult = data[1].trim()
                            stop = true
                        }
                    }

                }
            }
        } catch (t: Throwable) {
            hardwareResult = null
        }

        /**
         * Returns ARM32_BINDER64 if the OS supports only 32-bit but the hardware
         * supports 64-bit, otherwise returns the result from the software.
         */
        return if (softwareResult == ABI.ARM32 && (hardwareResult == "8" || hardwareResult.equals("aarch64", true))) {
            ArchitectureResult(ABI.ARM32_BINDER64)
        } else {
            softwareResult?.let { ArchitectureResult(it) }
        }

    }

}