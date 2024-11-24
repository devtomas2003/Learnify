package pt.spacelabs.experience.learnify

import DBHelper
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class Nearby : ComponentActivity() {
    private var lastSelectedTabId: Int = R.id.nav_profile
    private val REQUEST_ENABLE_BT = 1
    val devices = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.nearby)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = lastSelectedTabId

        val bAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bAdapter == null) {
            Toast.makeText(applicationContext, "Bluetooth Not Supported", Toast.LENGTH_SHORT).show()
        } else {
            enableDevicesDiscover()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.listNearby)
        val btnScan = findViewById<Button>(R.id.btnScan)

        btnScan.setOnClickListener {
            enableDevicesDiscover()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = NearbyAdapter(devices)


        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_courses -> {
                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_exit -> {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Confirmação")
                    builder.setMessage("Tem certeza de que deseja sair?")

                    builder.setPositiveButton("Sim") { dialog, _ ->
                        DBHelper(this).clearConfig("auth")
                        val intent = Intent(this, Welcome::class.java)
                        startActivity(intent)
                        finish()
                        dialog.dismiss()
                    }

                    builder.setNegativeButton("Não") { dialog, _ ->
                        bottomNavigationView.selectedItemId = R.id.nav_profile
                        dialog.dismiss()
                    }

                    builder.create().show()
                    true
                }
                else -> false
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                enableDevicesDiscover()
            } else {
                Toast.makeText(applicationContext, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enableDevicesDiscover() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            val bAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bAdapter != null) {
                if (!bAdapter.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                } else {
                    val pairedDevices = bAdapter.bondedDevices
                    devices.clear()
                    if (pairedDevices.isNotEmpty()) {
                        for (device in pairedDevices) {
                            val deviceName = device.name
                            val macAddress = device.address
                            devices.add("$deviceName ($macAddress)")
                        }
                        val recyclerView = findViewById<RecyclerView>(R.id.listNearby)
                        val adapter = recyclerView.adapter
                        if (adapter != null && adapter is NearbyAdapter) {
                            adapter.notifyDataSetChanged()
                        } else {
                            Log.e("Nearby", "RecyclerView adapter is not set or is null")
                        }
                    }
                }
            }
        }
    }

}