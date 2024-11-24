package pt.spacelabs.experience.learnify

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Permissions : ComponentActivity() {

    private val requiredPermissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private val permissionDescriptions = mapOf(
        android.Manifest.permission.CAMERA to "Acesso à câmera para ler QRCodes",
        android.Manifest.permission.BLUETOOTH_CONNECT to "Ligar a amigos via NearBy",
        android.Manifest.permission.POST_NOTIFICATIONS to "Enviar Notificações sobre a APP"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.permissions)
        enableEdgeToEdge()

        if (areAllPermissionsGranted()) {
            onPermissionsGranted()
        } else {
            findViewById<Button>(R.id.btnPermissions).setOnClickListener {
                permissionLauncher.launch(requiredPermissions)
            }
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val deniedPermissions = permissions.filter { !it.value }.keys

            if (deniedPermissions.isEmpty()) {
                onPermissionsGranted()
            } else {
                onPermissionsDenied(deniedPermissions)
            }
        }

    private fun areAllPermissionsGranted(): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    private fun onPermissionsDenied(deniedPermissions: Set<String>) {
        val txtErrorPerms = findViewById<TextView>(R.id.txtRequestPerms)
        txtErrorPerms.visibility = View.VISIBLE

        val deniedDescriptions = deniedPermissions.mapNotNull { permissionDescriptions[it] }
        val recyclerView = findViewById<RecyclerView>(R.id.rvPermissions)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PermissionAdapter(deniedDescriptions)
    }

    private fun onPermissionsGranted() {
        val intent = Intent(this, Dashboard::class.java)
        startActivity(intent)
        finish()
    }
}