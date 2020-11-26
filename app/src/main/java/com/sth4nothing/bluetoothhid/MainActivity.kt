package com.sth4nothing.bluetoothhid

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sth4nothing.bluetoothhid.listeners.*
import com.sth4nothing.bluetoothhid.senders.KeyboardSender
import com.sth4nothing.bluetoothhid.senders.RelativeMouseSender
import com.sth4nothing.bluetoothhid.ui.dashboard.DashboardFragment
import com.sth4nothing.bluetoothhid.ui.home.HomeFragment
import com.sth4nothing.bluetoothhid.ui.home.HomeViewModel
import com.sth4nothing.bluetoothhid.ui.settings.SettingsFragment
import com.sth4nothing.bluetoothhid.utils.CustomGestureDetector
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var sharedPref: SharedPreferences
    lateinit var homeViewModel: HomeViewModel
    var rMouseSender: RelativeMouseSender? = null
    var rKeyboardSender: KeyboardSender? = null
    var composite: CompositeListener? = null
    var host: BluetoothDevice? = null
    var hidDevice: BluetoothHidDevice? = null
    val homeFragment: HomeFragment?
        get() = getFragment()

    val dashboardFragment: DashboardFragment?
        get() = getFragment()

    val settingsFragment: SettingsFragment?
        get() = getFragment()

    private inline fun <reified T : Fragment> getFragment(): T? {
        val fragment =
            supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.primaryNavigationFragment
        return if (fragment != null && fragment.isVisible && fragment is T) fragment else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null && !adapter.isEnabled) {
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
        }
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        sharedPref = this.getPreferences(MODE_PRIVATE)
        sharedPref.apply { }
        homeViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(HomeViewModel::class.java)
        homeViewModel.bltConnect.postValue(false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onStart() {
        super.onStart()
        if (sharedPref.getBoolean(getString(R.string.screen_on_flag), true)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        toast("${BuildConfig.BUILD_TYPE}-${Build.BRAND} ${Build.MODEL}")
        BluetoothController.autoPairFlag =
            sharedPref.getBoolean(getString(R.string.auto_pair_flag), true)
        if (!BuildConfig.DEBUG || Build.TYPE.toLowerCase(Locale.getDefault()) != "userdebug") {
            BluetoothController.init(applicationContext)
        }
        BluetoothController.getSender { hidDevice, host ->
            Log.wtf("weiufhas", "Callback called")
            this.hidDevice = hidDevice
            this.host = host
            val mainHandler = Handler(mainLooper)

            mainHandler.post {
                rKeyboardSender = KeyboardSender(hidDevice, host)


                rMouseSender = RelativeMouseSender(hidDevice, host)
                Log.i("TAG###UI", Thread.currentThread().name);
                val viewTouchListener = ViewListener(hidDevice, host, rMouseSender!!)
                val mDetector =
                    CustomGestureDetector(applicationContext, GestureDetectListener(rMouseSender!!))

                val gTouchListener =
                    View.OnTouchListener { _, event -> mDetector.onTouchEvent(event) }


                composite = CompositeListener()
                composite?.registerListener(gTouchListener)
                composite?.registerListener(viewTouchListener)

                homeFragment?.trackPad?.setOnTouchListener(composite)
                homeViewModel.bltConnect.postValue(true)
            }

            Log.i("TAG###", Thread.currentThread().name);
        }
        BluetoothController.getDisconnector {
            val mainHandler = Handler(mainLooper)

            mainHandler.post {
                homeViewModel.bltConnect.postValue(true)
                composite = null
                hidDevice = null
                host = null
                rKeyboardSender = null
                rMouseSender = null
                homeFragment?.trackPad?.setOnTouchListener(null)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean =
        rKeyboardSender != null
                && event != null
                && homeFragment != null
                && rKeyboardSender!!.sendKeyboard(
            keyCode,
            event,
            homeViewModel.modifier_checked_state.value!!
        )
                || super.onKeyDown(keyCode, event)

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            else -> super.onOptionsItemSelected(item)
        }

    fun toast(s: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(applicationContext, s, duration).show()
    }

    companion object {
        const val REQUEST_ENABLE_BT = 1001
    }
}