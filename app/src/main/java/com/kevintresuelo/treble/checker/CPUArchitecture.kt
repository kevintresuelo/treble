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

import android.os.Binder
import android.os.Build

data class ArchitectureResult(
    val cpuArch: ABI
)

enum class ABI {
    ARM,
    ARM32_BINDER64,
    ARM64,
    X86,
    X86_64
}

object CPUArchitecture {

    fun check(): ArchitectureResult? {

        /**
         * Initializes the software-supported ABI, which is the preferred ABI and
         * is listed first in Build.SUPPORTED_ABIS
         */
        val supportedABIs = Build.SUPPORTED_ABIS
        val cpuArch = supportedABIs.first()

        return when {
            cpuArch.contains("arm64-v8a") -> ArchitectureResult(ABI.ARM64)
            cpuArch.contains("armeabi-v7a") -> ArchitectureResult(ABI.ARM)
            cpuArch.contains("x86_64") -> ArchitectureResult(ABI.X86_64)
            cpuArch.contains("x86") -> ArchitectureResult(ABI.X86)
            else -> null
        }

        // TODO: Implement whether CPU is ARM32_BINDER64

    }

}