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
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kevintresuelo.novus.UpdateChecker
import com.kevintresuelo.treble.BuildConfig
import com.kevintresuelo.treble.R
import com.kevintresuelo.treble.databinding.DialogContributorsListBinding
import com.kevintresuelo.treble.databinding.FragmentAboutBinding
import com.kevintresuelo.treble.donate.DonateDialogFragment
import com.kevintresuelo.treble.utils.AppInfo
import com.kevintresuelo.treble.utils.openUrl
import com.kevintresuelo.treble.utils.toOrdinal
import kotlinx.android.synthetic.main.list_item_contributor.view.*
import java.util.*

class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding

    private lateinit var contributorsBinding: DialogContributorsListBinding
    private val contributorsList = ArrayList<Contributor>()
    private val contributorsListAdapter = ContributorsListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /**
         * Initializes data binding with the layout and view container.
         */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false)

        /**
         * Shows the current version of the app to the appropriate section in the
         * AboutFragment.
         */
        binding.faTvAppInfoDetailSupplementary.text = getString(R.string.about_app_version_pattern, BuildConfig.VERSION_NAME)

        /**
         * Opens the store listing once the app info section of the AboutFragment
         * is clicked.
         */
        binding.faLlAppInfo.setOnClickListener{
            openStoreListing()
        }

        /**
         * Opens an AlertDialog to show the recent changes of the app.
         */
        // TODO: Implement a system-wide and much more reliable changelog interface
        val changelogDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.changelog_title))
            .setMessage(getString(R.string.changelog_message))
            .setPositiveButton(getString(R.string.changelog_action), null)
        binding.faLlAppChangelog.setOnClickListener {
            changelogDialog.show()
        }

        /**
         * Sets up the list of contributors, and the RecyclerView
         */
        contributorsBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_contributors_list, container, false)
        /**
         * List of contributors
         */
        contributorsList.add(Contributor("Alexey", Locale("ru").displayName))
        contributorsList.add(Contributor("Gabor Sari", Locale("hu").displayName))
        contributorsList.add(Contributor("Hüseyin", Locale("tr").displayName))
        contributorsList.add(Contributor("hjthjthjt", Locale.SIMPLIFIED_CHINESE.displayName))
        contributorsList.add(Contributor("Julio Gonzales", Locale("es").displayName))
        contributorsList.add(Contributor("Melvin Salas", Locale("es").displayName))
        contributorsList.add(Contributor("Nicolas", Locale("fr").displayName))
        contributorsList.add(Contributor("Rob", Locale("it").displayName))
        contributorsList.add(Contributor("Sean Kuan", Locale.TRADITIONAL_CHINESE.displayName))
        contributorsList.add(Contributor("Svegmen925", Locale("ru").displayName))
        contributorsList.add(Contributor("Vojtěch Vokoun", Locale("cs").displayName))

        contributorsListAdapter.setContributorsList(contributorsList)

        contributorsBinding.dclRvContributorsList.layoutManager = LinearLayoutManager(requireContext())
        contributorsBinding.dclRvContributorsList.adapter = contributorsListAdapter

        val contributorDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.contributors_title)
            .setView(contributorsBinding.root)
            .setPositiveButton(getString(R.string.contributors_action), null)

        /**
         * Opens an AlertDialog to show the list of contributors of the app.
         */
        binding.faLlAppContributors.setOnClickListener {
            if (contributorsBinding.root.parent != null) {
                (contributorsBinding.root.parent as ViewGroup).removeView(contributorsBinding.root)
            }

            contributorDialog.show()
        }

        /**
         * Opens the app's Twitter profile.
         */
        binding.faLlAppTwitter.setOnClickListener {
            openUrl(activity as Activity, "https://twitter.com/treblecheckapp", false)
        }

        /**
         * Opens the app's Github profile.
         */
        binding.faLlAppGithub.setOnClickListener {
            openUrl(activity as Activity, "https://github.com/kevintresuelo/treble", false)
        }

        /**
         * Shows the licenses for the libraries used, hidden at the moment as no
         * third party library is used.
         */
        // TODO: Implement a system-wide and much more reliable licenses interface
        binding.faLlAppLicenses.setOnClickListener(null)

        /**
         * Checks for available updates in the Google Play Store
         */
        binding.faLlAppUpdater.setOnClickListener {
            val updateChecker = UpdateChecker(activity as Activity, requireActivity().findViewById(android.R.id.content), true)
            updateChecker.checkForUpdates()
        }

        /**
         * Winks at the user once the developer's name is clicked.
         */
        binding.faLlDevInfo.setOnClickListener {
            Toast.makeText(context, "( ゝ‿◕)", Toast.LENGTH_SHORT).show()
        }
        binding.faLlDevInfo.setOnLongClickListener {
            Toast.makeText(context, "(╯°▽°)╯ ┻━┻", Toast.LENGTH_SHORT).show()
            true
        }

        /**
         * Drafts an email to be sent to the developer's email address.
         */
        binding.faLlDevEmail.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:kevztresuelo@gmail.com")
            openUrl(activity as Activity, emailIntent, false)
        }

        /**
         * Opens the developer's Facebook profile.
         */
        binding.faLlDevFacebook.setOnClickListener {
            openUrl(activity as Activity, "https://www.facebook.com/KevinTresuelo", false)
        }

        /**
         * Opens the developer's Instagram profile.
         */
        binding.faLlDevInstagram.setOnClickListener {
            openUrl(activity as Activity, "https://www.instagram.com/kevintresuelo", false)
        }

        /**
         * Opens the developer's Twitter profile.
         */
        binding.faLlDevTwitter.setOnClickListener {
            openUrl(activity as Activity, "https://twitter.com/kevintresuelo", false)
        }

        /**
         * Opens the developer's Telegram profile.
         */
        binding.faLlDevTelegram.setOnClickListener {
            openUrl(activity as Activity, "https://t.me/kevintresuelo", false)
        }

        /**
         * Opens the developer's Google Play profile.
         */
        binding.faLlDevGooglePlay.setOnClickListener {
            openUrl(activity as Activity, "https://play.google.com/store/apps/dev?id=6581589998869233711", false)
        }

        /**
         * Opens the store listing for the user to rate on the app.
         */
        binding.faLlOthersRate.setOnClickListener {
            openStoreListing()
        }

        /**
         * Opens the translation dashboard
         */
        binding.faLlOthersTranslate.setOnClickListener {
            openUrl(activity as Activity, "https://poeditor.com/join/project/oVDSgMHUwC", false)
        }

        /**
         * Opens the donation dialog
         */
        binding.faLlOthersDonate.setOnClickListener {
            DonateDialogFragment().show(parentFragmentManager, DonateDialogFragment.TAG)
        }

        /**
         * Initializes current day, month and year for the calculation of the age
         * of the app and the developer.
         */
        val nowYear = Calendar.getInstance().get(Calendar.YEAR)
        val nowMonth = Calendar.getInstance().get(Calendar.MONTH)
        val nowDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        /**
         * Checks if today is the app's birthday, then displays its age.
         */
        if (nowMonth == (AppInfo.APP_RELEASE_MONTH-1) && nowDay == AppInfo.APP_RELEASE_DAY) {
            val age = (nowYear - AppInfo.APP_RELEASE_YEAR)
            if (age > 0) {
                binding.faTvAppInfoDetailSupplementary.text = getString(R.string.about_app_birthday_pattern, toOrdinal(age))
            }
        }

        /**
         * Checks if today is a leap day, or today is March 1, as per the
         * developer's birthday, then displays his age.
         */
        if (((nowYear % 4 == 0) && (nowYear % 100!= 0)) || (nowYear % 400 == 0)) {
            if (nowMonth == (AppInfo.DEV_BIRTH_MONTH-1) && nowDay == AppInfo.DEV_BIRTH_DAY) {
                val age = (nowYear - AppInfo.DEV_BIRTH_YEAR)
                if (age > 0) {
                    binding.faTvDevInfoDetailSupplementary.text = getString(R.string.about_dev_info_detail_birthday_pattern, toOrdinal(age))
                }
            }
        } else {
            if (nowMonth == (AppInfo.DEV_BIRTH_NOLEAP_MONTH-1) && nowDay == AppInfo.DEV_BIRTH_NOLEAP_DAY) {
                val age = (nowYear - AppInfo.DEV_BIRTH_YEAR)
                if (age > 0) {
                    binding.faTvDevInfoDetailSupplementary.text = getString(R.string.about_dev_info_detail_birthday_pattern, toOrdinal(age))
                }
            }
        }

        /**
         * Returns the root element of this inflated layout for the fragment to handle
         */
        return binding.root
    }

    /**
     * Opens the store listing of this app, falls back to the browser based
     * page if market app is not found.
     */
    private fun openStoreListing() {
        val appPackageName = context?.packageName // getPackageName() from Context or Activity object
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (anfe: android.content.ActivityNotFoundException) {
            openUrl(activity as Activity, "https://play.google.com/store/apps/details?id=$appPackageName", false)
        }
    }

    /**
     * RecyclerView.Adapter for showing a list of contributors
     */
    class ContributorsListAdapter : RecyclerView.Adapter<ContributorsListAdapter.ContributorsViewHolder>() {

        private var contributorsList = emptyList<Contributor>()

        override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): ContributorsViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_contributor, parent, false)
            return ContributorsViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ContributorsListAdapter.ContributorsViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        override fun getItemCount() = contributorsList.size

        fun setContributorsList(list: List<Contributor>) {
            if (list != contributorsList) {
                contributorsList = list
                notifyDataSetChanged()
            }
        }

        private fun getItem(position: Int) = if (contributorsList.isEmpty()) null else contributorsList[position]

        inner class ContributorsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bind(item: Contributor?) {
                item?.apply {
                    itemView.apply {
                        lic_tv_contributor_name.text = name
                        lic_tv_contributor_description.text = resources.getString(R.string.contributors_description_translation, description)
                    }
                }
            }
        }

    }

    data class Contributor (
        val name: String,
        val description: String?
    )

}
