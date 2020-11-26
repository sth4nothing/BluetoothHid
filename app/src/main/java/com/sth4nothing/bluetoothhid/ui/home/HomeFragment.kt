package com.sth4nothing.bluetoothhid.ui.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sth4nothing.bluetoothhid.MainActivity
import com.sth4nothing.bluetoothhid.R

class HomeFragment : Fragment() {
    lateinit var homeViewModel: HomeViewModel
    lateinit var bluetoothStatus: MenuItem
    lateinit var sharedPref: SharedPreferences
    lateinit var trackPad: TextView
    lateinit var parent: MainActivity
    lateinit var hidDevice: BluetoothHidDevice
    lateinit var host: BluetoothDevice

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parent = requireActivity() as MainActivity
//        parent.supportActionBar?.show()
        homeViewModel = ViewModelProvider(
            parent,
            ViewModelProvider.NewInstanceFactory()
        ).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        trackPad = root.findViewById(R.id.trackpad)
        sharedPref = parent.getPreferences(Context.MODE_PRIVATE)
        connectedString = parent.getString(R.string.connected)
        disconnectedString = parent.getString(R.string.not_connected)
        parent.supportActionBar?.show()
        setHasOptionsMenu(true)
        if (parent.composite != null) {
            trackPad.setOnTouchListener(parent.composite)
        }
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_menu, menu)
        bluetoothStatus = menu.findItem(R.id.ble_app_connection_status)
        menu.findItem(R.id.check_modifier_state).title =
            if (homeViewModel.modifier_checked_state.value == 1) "(P)" else "(N)"
        homeViewModel.bltConnect.observe(viewLifecycleOwner, {
            updateStatus(it)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.check_modifier_state -> {
            if (homeViewModel.modifier_checked_state.value == 1) {
                homeViewModel.modifier_checked_state.postValue(0)
                item.title = "(N)"
                parent.rKeyboardSender?.sendNullKeys()
            } else {
                homeViewModel.modifier_checked_state.postValue(1)
                item.title = "(P)"
            }
            true
        }
        R.id.action_keyboard -> {
            val imm = parent.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    lateinit var connectedString: String
    lateinit var disconnectedString: String

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateStatus(connected: Boolean) {
        if (connected) {
            bluetoothStatus.icon = parent.getDrawable(R.drawable.ic_action_app_connected)
            bluetoothStatus.tooltipText = connectedString
        } else {
            bluetoothStatus.icon = parent.getDrawable(R.drawable.ic_action_app_not_connected)
            bluetoothStatus.tooltipText = disconnectedString
        }
    }
}