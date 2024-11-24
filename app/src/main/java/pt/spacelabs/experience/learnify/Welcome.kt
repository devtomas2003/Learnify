package pt.spacelabs.experience.learnify

import DBHelper
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Welcome : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.welcome)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val token = DBHelper(this).getConfig("auth")

        if(token != "none"){
            val requestQueue: RequestQueue = Volley.newRequestQueue(this)

            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.loading, null)
            dialogBuilder.setView(dialogView)
            val alertDialog: AlertDialog = dialogBuilder.create()
            alertDialog.show()

            val stringRequest = object : StringRequest(
                Method.GET,
                "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Private/PingAuth",
                Response.Listener { _ ->
                    val intent = Intent(this, Permissions::class.java)
                    startActivity(intent)
                    finish()
                },
                Response.ErrorListener { error ->
                    try {
                        val errorResponse = String(error.networkResponse.data, Charsets.UTF_8)
                        val errorObject = JSONObject(errorResponse)
                        if (errorObject.has("Errors")) {
                            DBHelper(this).clearConfig("auth")
                            DBHelper(this).clearConfig("username")
                            DBHelper(this).clearConfig("email")
                            alertDialog.dismiss()
                        }
                    } catch (e: Exception) {
                        val intent = Intent(this, Permissions::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            ) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    val auth = "Bearer " + DBHelper(this@Welcome).getConfig("auth")
                    headers["Authorization"] = auth
                    return headers
                }
            }

            stringRequest.retryPolicy = DefaultRetryPolicy(
                2000,
                2,
                1.0f
            )

            requestQueue.add(stringRequest)
        }

        val btnStartSession = findViewById<TextView>(R.id.btnStartSession)

        btnStartSession.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        val btnCreateAccount = findViewById<TextView>(R.id.btnCreateAccount)

        btnCreateAccount.setOnClickListener {
            val intent = Intent(this, CreateAccount::class.java)
            startActivity(intent)
            finish()
        }
    }
}