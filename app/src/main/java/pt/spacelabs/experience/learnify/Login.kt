package pt.spacelabs.experience.learnify

import DBHelper
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class Login : ComponentActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.login)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val goBackLink = findViewById<TextView>(R.id.goBackLink)

        goBackLink.setOnClickListener {
            val intent = Intent(this, Welcome::class.java)
            startActivity(intent)
            finish()
        }

        val btnLogin = findViewById<TextView>(R.id.btnStartSession)

        val inpEmail = findViewById<EditText>(R.id.inpMail)
        val inpPass = findViewById<EditText>(R.id.inpPass)
        
        btnLogin.setOnClickListener {
            if(checkFields(inpEmail.text.toString(), inpPass.text.toString())){
                val requestQueue: RequestQueue = Volley.newRequestQueue(this)

                val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
                val inflater = this.layoutInflater
                val dialogView: View = inflater.inflate(R.layout.loading, null)
                dialogBuilder.setView(dialogView)
                val alertDialog: AlertDialog = dialogBuilder.create()
                alertDialog.show()

                val stringRequest = object : StringRequest(
                    Method.POST,
                    "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Auth/Login",
                    Response.Listener { response ->
                        alertDialog.dismiss()
                        try {
                            val jsonObject = JSONObject(response)
                            if (jsonObject.has("Token")) {
                                val token = jsonObject.getString("Token")
                                DBHelper(this).createConfig("auth", token)
                                val intent = Intent(this, Permissions::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } catch (e: JSONException) {
                            inpEmail.setText("")
                            inpPass.setText("")
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
                                inpEmail.setText("")
                                inpPass.setText("")
                                AlertDialog.Builder(this)
                                    .setTitle("Ocorreu um erro")
                                    .setMessage(errorMessage)
                                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                    .create()
                                    .show()
                            }
                        } catch (e: Exception) {
                            inpEmail.setText("")
                            inpPass.setText("")
                            AlertDialog.Builder(this)
                                .setTitle("Falha de ligação")
                                .setMessage("Para usares esta funcionalidade, verifica a tua ligação!")
                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                .create()
                                .show()
                        }
                    }
                ) {
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        val auth = "Basic " + Base64.encodeToString("${inpEmail.text}:${inpPass.text}".toByteArray(), Base64.NO_WRAP)
                        headers["Authorization"] = auth
                        return headers
                    }
                    override fun getBody(): ByteArray {
                        return "{studentId: 0}".toByteArray()
                    }
                    override fun getBodyContentType(): String {
                        return "text/plain; charset=UTF-8"
                    }
                }

                requestQueue.add(stringRequest)
            }
        }
    }

    private fun checkFields(txtMail: String, txtPassword: String): Boolean {
        if(txtMail.isEmpty() || !txtMail.contains("@")){
            AlertDialog.Builder(this)
                .setTitle("Erro")
                .setMessage("Por favor, coloque o seu email!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            return false
        }
        if(txtPassword.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Erro")
                .setMessage("Por favor, preencha a password!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            return false
        }
        return true
    }
}