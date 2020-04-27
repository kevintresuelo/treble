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

package com.kevintresuelo.treble.screens

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kevintresuelo.adnoto.RatePrompter
import com.kevintresuelo.treble.R
import com.kevintresuelo.treble.checker.*
import com.kevintresuelo.treble.databinding.FragmentCheckerBinding
import com.kevintresuelo.treble.donate.DonateDialogFragment
import com.kevintresuelo.treble.utils.openUrl


class CheckerFragment : Fragment() {

    private lateinit var binding: FragmentCheckerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /**
         * Initializes data binding with the layout and view container.
         */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_checker, container, false)

        /**
         * Notifies the host activity that this fragment has options menu.
         */
        setHasOptionsMenu(true)

        /**
         * Asks the user to rate the app on Google Play
         */
        RatePrompter(context, 3)

        /**
         * Shows the Donate CardView if the user has opened the app thrice
         * already, and hasn't dismissed the card yet.
         */
        val prefsFileKey = "prompt_donate"
        val hasDismissedKey = "donate_card_dismissed"
        val timesAppOpenedKey = "donate_card_times_app_opened"
        
        val sharedPreferences = activity?.getSharedPreferences(prefsFileKey, Context.MODE_PRIVATE)
        sharedPreferences?.let { sharedPrefs ->
            val isDonateCardAlreadyDismissed = sharedPrefs.getBoolean(hasDismissedKey, false)
            if (!isDonateCardAlreadyDismissed) {
                val timesAppOpened = sharedPrefs.getInt(timesAppOpenedKey, 0) + 1  // timesAppOpened + 1 more for this instance
                if (timesAppOpened >= 3) {
                    binding.fcMcvStatusDonate.visibility = View.VISIBLE
                    binding.fcMbStatusDonateAction1.setOnClickListener {
                        DonateDialogFragment().show(parentFragmentManager, DonateDialogFragment.TAG)
                        sharedPrefs.edit()?.putBoolean(hasDismissedKey, true)?.apply()
                        binding.fcMcvStatusDonate.visibility = View.GONE
                    }
                    binding.fcMbStatusDonateAction2.setOnClickListener {
                        sharedPrefs.edit()?.putBoolean(hasDismissedKey, true)?.apply()
                        binding.fcMcvStatusDonate.visibility = View.GONE
                    }
                } else {
                    sharedPrefs.edit()?.putInt(timesAppOpenedKey, timesAppOpened)?.apply()
                }
            }
        }

        /**
         * Interprets the result of Project Treble status of the device, then
         * shows the appropriate response on the UI.
         */
        val trebleCheck = Treble.check()
        trebleCheck?.let {
            binding.fcIvStatusTrebleIcon.setImageResource(R.drawable.ic_check_circle)
            binding.fcTvStatusTrebleSubtitle.setText(R.string.checker_global_supported_subtitle)
            val vndkVersion = "${ if (it.isVndkLite) "Lite " else ""}${it.vndkVersion ?: ""}"
            binding.fcTvStatusTrebleSupportingText.text = getString(
                if (it.isTrebleLegacy)
                    R.string.checker_treble_supported_old_vndk_supporting_text
                else
                    R.string.checker_treble_supported_new_vndk_supporting_text,
                vndkVersion
            )
        }

        /**
         * Shows more info regarding Project Treble, and what it implies to the
         * device of the user.
         */
        binding.fcMbStatusTrebleAction1.setOnClickListener {
            if (binding.fcTvStatusTrebleDetailText.isVisible) {
                collapse(binding.fcTvStatusTrebleDetailText)
                binding.fcMbStatusTrebleAction1.text = getString(R.string.checker_global_action_more_title)
            } else {
                expand(binding.fcTvStatusTrebleDetailText)
                binding.fcMbStatusTrebleAction1.text = getString(R.string.checker_global_action_less_title)
            }
        }

        /**
         * Interprets the result of A/B Partition, then shows the appropriate
         * response on the UI.
         */
        val abCheck = AB.check()
        abCheck?.let {
            binding.fcIvStatusAbPartitionIcon.setImageResource(R.drawable.ic_download)
            binding.fcTvStatusAbPartitionSubtitle.text = getString(R.string.checker_global_supported_subtitle)
            binding.fcTvStatusAbPartitionSupportingText.text = getString(
                if (it.isVirtual) {
                    R.string.checker_ab_supported_virtual_supporting_text
                } else {
                    R.string.checker_ab_supported_supporting_text
                }
            )
            binding.fcTvStatusAbPartitionDetailText.text = getString(R.string.checker_ab_supported_detail_text)
        }

        /**
         * Shows more info regarding the A/B Partitions, and what it implies to
         * the device of the user.
         */
        binding.fcMbStatusAbPartitionAction1.setOnClickListener {
            if (binding.fcTvStatusAbPartitionDetailText.isVisible) {
                collapse(binding.fcTvStatusAbPartitionDetailText)
                binding.fcMbStatusAbPartitionAction1.text = getString(R.string.checker_global_action_more_title)
            } else {
                expand(binding.fcTvStatusAbPartitionDetailText)
                binding.fcMbStatusAbPartitionAction1.text = getString(R.string.checker_global_action_less_title)
            }
        }

        /**
         * Interprets the result of CPU Architecture, then shows the appropriate
         * response on the UI.
         */
        val cpuArchitectureCheck = CPUArchitecture.check()
        cpuArchitectureCheck?.let {
            val cpuArchDesc = when (it.cpuArch) {
                ABI.ARM32 -> R.string.checker_cpu_architecture_arm32_subtitle
                ABI.ARM32_BINDER64 -> R.string.checker_cpu_architecture_arm32_binder64_subtitle
                ABI.ARM64 -> R.string.checker_cpu_architecture_arm64_subtitle
                ABI.X86 -> R.string.checker_cpu_architecture_x86_subtitle
                ABI.X86_64 -> R.string.checker_cpu_architecture_x86_64_subtitle
            }
            binding.fcTvStatusCpuArchitectureSubtitle.text = getString(cpuArchDesc)
            binding.fcTvStatusCpuArchitectureSupportingText.text = getString(R.string.checker_cpu_architecture_known_supporting_text, getString(cpuArchDesc))

            if (it.cpuArch == ABI.ARM32 || it.cpuArch == ABI.ARM32_BINDER64) {
                binding.fcIvStatusCpuArchitectureIcon.setImageResource(R.drawable.ic_cpu_32_bit)
            } else if (it.cpuArch == ABI.ARM64) {
                binding.fcIvStatusCpuArchitectureIcon.setImageResource(R.drawable.ic_cpu_64_bit)
            }
        }

        /**
         * Interprets the result of CPU Architecture, then shows the appropriate
         * response on the UI.
         */
        val systemAsRootCheck = SystemAsRoot.check()
        systemAsRootCheck?.let {
            if (it) {
                binding.fcTvStatusSystemAsRootSubtitle.text = getString(R.string.checker_global_supported_subtitle)
                binding.fcTvStatusSystemAsRootSupportingText.text = getString(R.string.checker_system_as_root_supported_supporting_text)
            } else {
                binding.fcTvStatusSystemAsRootSubtitle.text = getString(R.string.checker_global_unsupported_subtitle)
                binding.fcTvStatusSystemAsRootSupportingText.text = getString(R.string.checker_system_as_root_unsupported_supporting_text)
            }
        }

        /**
         * Redirects users for more information regarding Project Treble to an
         * Android Developers blog post re: Project Treble.
         */
        binding.fcMbStatusExplainerAction1.setOnClickListener {
            openUrl(activity as Activity, "https://bit.ly/2zrPYDk", false)
        }

        /**
         * Returns the root element of this inflated layout for the fragment to
         * handle.
         */
        return binding.root
    }

    /**
     * Shows the specified view by animating and expanding the parent view to
     * make room for the child view.
     *
     * @param view: child view to be shown in the parent view
     */
    private fun expand(view: View) {
        val matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec((view.parent as View).width,View.MeasureSpec.EXACTLY)
        val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        val targetHeight = view.measuredHeight

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        view.layoutParams.height = 1
        val anim: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                view.visibility = View.VISIBLE
                view.layoutParams.height =
                    if (interpolatedTime == 1f)
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    else
                        (targetHeight * interpolatedTime).toInt()
                view.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        // Expansion speed of 1dp/ms
        anim.duration = ((targetHeight / view.context.resources.displayMetrics.density)*1.5f).toLong()
        view.startAnimation(anim)
    }

    /**
     * Hides the specified view by animating and collapsing the parent view.
     *
     * @param view: child view to be hidden from the parent view
     */
    private fun collapse(view: View) {
        val initialHeight = view.measuredHeight
        val anim: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float,t: Transformation) {
                if (interpolatedTime == 1f) {
                    view.visibility = View.GONE
                } else {
                    view.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                    view.requestLayout()
                }
            }
            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        // Collapse speed of 1dp/ms
        anim.duration = ((initialHeight / view.context.resources.displayMetrics.density)*1.5f).toLong()
        view.startAnimation(anim)
    }

    /**
     * Inflates the menu from R.menu.menu_checker resource
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_checker, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Navigates to specific fragments depending on the menu clicked
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mt_i_about -> this.findNavController().navigate(R.id.action_trebleFragment_to_aboutFragment)
        }
        return super.onOptionsItemSelected(item)
    }

}
