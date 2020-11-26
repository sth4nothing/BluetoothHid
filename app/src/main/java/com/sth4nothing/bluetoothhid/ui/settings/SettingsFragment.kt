package com.sth4nothing.bluetoothhid.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.sth4nothing.bluetoothhid.*

class SettingsFragment : Fragment() {
    lateinit var settingsViewModel: SettingsViewModel
    lateinit var sharedPref: SharedPreferences
    lateinit var disconnectButton: MaterialButton
    lateinit var autoPairSwitch: SwitchMaterial
    lateinit var screenOnSwitch: SwitchMaterial
    lateinit var parent: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parent = requireActivity() as MainActivity
        settingsViewModel = ViewModelProvider(
            parent,
            ViewModelProvider.NewInstanceFactory()
        ).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
//        parent.supportActionBar?.hide()
        disconnectButton = root.findViewById(R.id.disconnect_button)
        autoPairSwitch = root.findViewById(R.id.auto_pair_switch)
        screenOnSwitch = root.findViewById(R.id.screen_on_switch)
        sharedPref = parent.getPreferences(Context.MODE_PRIVATE)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!settingsViewModel.hasInit) {
            settingsViewModel.hasInit = true
            settingsViewModel.autoPair.value =
                sharedPref.getBoolean(getString(R.string.auto_pair_flag), true)
            settingsViewModel.screenOn.value =
                sharedPref.getBoolean(getString(R.string.screen_on_flag), true)
        }

        disconnectButton.setOnClickListener {
            BluetoothController.btHid?.disconnect(BluetoothController.hostDevice)
        }
        autoPairSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != settingsViewModel.autoPair.value) {
                settingsViewModel.autoPair.postValue(isChecked)
                BluetoothController.autoPairFlag = isChecked
                with(sharedPref.edit()) {
                    putBoolean(getString(R.string.auto_pair_flag), isChecked)
                    apply()
                }
            }
        }
        screenOnSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != settingsViewModel.screenOn.value) {
                if (isChecked != settingsViewModel.screenOn.value)
                    settingsViewModel.screenOn.postValue(isChecked)
                if (isChecked) {
                    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                with(sharedPref.edit()) {
                    putBoolean(getString(R.string.screen_on_flag), isChecked)
                    apply()
                }
            }
        }

        settingsViewModel.autoPair.observe(viewLifecycleOwner, {
            autoPairSwitch.isChecked = it
        })

        settingsViewModel.screenOn.observe(viewLifecycleOwner, {
            screenOnSwitch.isChecked = it
        })
    }
}