package pt.spacelabs.experience.learnify

import DBHelper
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException
import org.json.JSONObject

class Profile : ComponentActivity() {
    private var lastSelectedTabId: Int = R.id.nav_profile
    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.profile)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        findViewById<Button>(R.id.btnSearchFriends).setOnClickListener {
            val intent = Intent(this, Nearby::class.java)
            startActivity(intent)
            finish()
        }

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        registerNetworkCallback()

        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions

        val inpName = findViewById<EditText>(R.id.inpName)
        val inpEmail = findViewById<EditText>(R.id.inpMail)
        val inpPassword = findViewById<EditText>(R.id.inpPass)
        val inpRepass = findViewById<EditText>(R.id.inpRepass)

        inpName.setText(DBHelper(this).getConfig("username"))
        inpEmail.setText(DBHelper(this).getConfig("email"))

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = lastSelectedTabId

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
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

        fun isNetworkAvailable(): Boolean {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        findViewById<Button>(R.id.btnChangeData).setOnClickListener {
            if(isNetworkAvailable()){
                if(inpName.text.isEmpty()){
                    inpPassword.setText("")
                    inpRepass.setText("")
                    AlertDialog.Builder(this)
                        .setTitle("Erro")
                        .setMessage("Por favor, coloque o seu nome!")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }else{
                    if(inpEmail.text.isEmpty() || !inpEmail.text.contains("@")){
                        inpPassword.setText("")
                        inpRepass.setText("")
                        AlertDialog.Builder(this)
                            .setTitle("Erro")
                            .setMessage("Por favor, coloque o seu email!")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }else{
                        if(inpPassword.text.isNotEmpty() && inpPassword.text.length < 8){
                            inpPassword.setText("")
                            inpRepass.setText("")
                            AlertDialog.Builder(this)
                                .setTitle("Erro")
                                .setMessage("Por favor, crie uma password com pelo menos 8 caracteres!")
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()
                        }else{
                            if(inpPassword.text.toString() != inpRepass.text.toString()){
                                AlertDialog.Builder(this)
                                    .setTitle("Erro")
                                    .setMessage("As passwords não correspondem!")
                                    .setPositiveButton("OK") { dialog, _ ->
                                        inpPassword.setText("")
                                        inpRepass.setText("")
                                        dialog.dismiss()
                                    }
                                    .create()
                                    .show()
                            }else{
                                val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
                                val inflater = this.layoutInflater
                                val dialogView: View = inflater.inflate(R.layout.loading, null)
                                dialogBuilder.setView(dialogView)
                                val alertDialog: AlertDialog = dialogBuilder.create()
                                alertDialog.show()

                                val requestQueue: RequestQueue = Volley.newRequestQueue(this)

                                val stringRequest = object : StringRequest(
                                    Method.POST,
                                    "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Private/UpdateProfile",
                                    Response.Listener { response ->
                                        alertDialog.dismiss()
                                        try {
                                            val jsonObject = JSONObject(response)
                                            val status = jsonObject.getString("IsSuccess").toBoolean()
                                            if(status){
                                                DBHelper(this).updateConfig("username", inpName.text.toString())
                                                DBHelper(this).updateConfig("email", inpEmail.text.toString())

                                                AlertDialog.Builder(this)
                                                    .setTitle("Sucesso")
                                                    .setMessage("Dados alterados com sucesso!")
                                                    .setPositiveButton("OK") { dialog, _ ->
                                                        inpPassword.setText("")
                                                        inpRepass.setText("")
                                                        dialog.dismiss()
                                                    }
                                                    .create()
                                                    .show()
                                            }else{
                                                inpPassword.setText("")
                                                inpRepass.setText("")
                                                AlertDialog.Builder(this)
                                                    .setTitle("Ocorreu um erro")
                                                    .setMessage(jsonObject.getString("Message"))
                                                    .setPositiveButton("OK") { dialog, _ ->
                                                        dialog.dismiss()
                                                    }
                                                    .create()
                                                    .show()
                                            }
                                        } catch (e: JSONException) {
                                            inpPassword.setText("")
                                            inpRepass.setText("")
                                            AlertDialog.Builder(this)
                                                .setTitle("Falha de ligação")
                                                .setMessage("Ocorreu um erro com a resposta do servidor!")
                                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                                .create()
                                                .show()
                                        }
                                    },
                                    Response.ErrorListener { error ->
                                        alertDialog.dismiss()
                                        try {
                                            val errorResponse = String(error.networkResponse.data, Charsets.UTF_8)
                                            val errorObject = JSONObject(errorResponse)
                                            if (errorObject.has("Errors")) {
                                                val errors = errorObject.getJSONArray("Errors")
                                                val errorMessage = errors.getString(0)
                                                inpPassword.setText("")
                                                inpRepass.setText("")
                                                AlertDialog.Builder(this)
                                                    .setTitle("Ocorreu um erro")
                                                    .setMessage(errorMessage)
                                                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                                    .create()
                                                    .show()
                                            }
                                        } catch (e: Exception) {
                                            inpPassword.setText("")
                                            inpRepass.setText("")
                                            AlertDialog.Builder(this)
                                                .setTitle("Falha de ligação")
                                                .setMessage("Para usares esta funcionalidade, verifica a tua ligação!")
                                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                                .create()
                                                .show()
                                        }
                                    }
                                ) {
                                    override fun getBody(): ByteArray {
                                        val body = JSONObject()
                                        body.put("Name", inpName.text.toString())
                                        body.put("Email", inpEmail.text.toString())
                                        body.put("Password", inpPassword.text.toString())
                                        return body.toString().toByteArray(Charsets.UTF_8)
                                    }
                                    override fun getHeaders(): Map<String, String> {
                                        val headers = HashMap<String, String>()
                                        val auth = "Bearer " + DBHelper(this@Profile).getConfig("auth")
                                        headers["Authorization"] = auth
                                        return headers
                                    }
                                    override fun getBodyContentType(): String {
                                        return "application/json; charset=UTF-8"
                                    }
                                }

                                requestQueue.add(stringRequest)
                            }
                        }
                    }
                }
            }else{
                if(inpPassword.text.isNotEmpty()){
                    AlertDialog.Builder(this)
                        .setTitle("Falha de ligação")
                        .setMessage("Para alterares a tua password, verifica a tua ligação!")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
                }else{
                    DBHelper(this).updateConfig("username", inpName.text.toString())
                    DBHelper(this).updateConfig("email", inpEmail.text.toString())
                    DBHelper(this).createConfig("haveToUpdateProfile", "yes")
                    AlertDialog.Builder(this)
                        .setTitle("Sucesso")
                        .setMessage("Dados alterados com sucesso!")
                        .setPositiveButton("OK") { dialog, _ ->
                            inpPassword.setText("")
                            inpRepass.setText("")
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            }
        }
    }
    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                runOnUiThread {
                    updateBehind()
                }
            }
        }

        connectivityManager.registerNetworkCallback(request, networkCallback!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
    }

    override fun onResume() {
        super.onResume()
        updateBehind()
    }

    fun updateBehind(){
        if(DBHelper(this@Profile).getConfig("haveToUpdateProfile") == "yes") {
            val requestQueue: RequestQueue = Volley.newRequestQueue(this@Profile)

            val stringRequest = object : StringRequest(
                Method.POST,
                "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Private/UpdateProfile",
                Response.Listener { response ->
                    try {
                        val jsonObject = JSONObject(response)
                        val status = jsonObject.getString("IsSuccess").toBoolean()
                        if (!status) {
                            AlertDialog.Builder(this@Profile)
                                .setTitle("Ocorreu um erro")
                                .setMessage(jsonObject.getString("Message"))
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()
                        }else{
                            DBHelper(this@Profile).clearConfig("haveToUpdateProfile")
                        }
                    } catch (e: JSONException) {
                    }
                },
                Response.ErrorListener { }
            ) {
                override fun getBody(): ByteArray {
                    val body = JSONObject()
                    body.put("Name", DBHelper(this@Profile).getConfig("username"))
                    body.put("Email", DBHelper(this@Profile).getConfig("email"))
                    return body.toString().toByteArray(Charsets.UTF_8)
                }

                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    val auth = "Bearer " + DBHelper(this@Profile).getConfig("auth")
                    headers["Authorization"] = auth
                    return headers
                }

                override fun getBodyContentType(): String {
                    return "application/json; charset=UTF-8"
                }
            }

            requestQueue.add(stringRequest)
        }
    }
}