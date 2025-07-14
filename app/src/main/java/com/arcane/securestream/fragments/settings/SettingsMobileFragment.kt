package com.arcane.securestream.fragments.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.auth.FirebaseAuth
import com.arcane.securestream.R
import com.arcane.securestream.activities.main.auth.LoginActivity

class SettingsMobileFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_mobile, rootKey)

        displaySettings()
    }

    private fun displaySettings() {
        findPreference<Preference>("p_settings_about")?.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(
                    SettingsMobileFragmentDirections.actionSettingsToSettingsAbout()
                )
                true
            }
        }

        findPreference<Preference>("p_settings_help")?.apply {
            setOnPreferenceClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/ar3xh")
                    )
                )
                true
            }
        }
        findPreference<Preference>("p_settings_logout")?.apply {
            setOnPreferenceClickListener {
                logoutUser()
                true
            }
        }
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
