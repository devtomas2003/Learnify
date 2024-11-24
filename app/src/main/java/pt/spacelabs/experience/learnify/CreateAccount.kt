package pt.spacelabs.experience.learnify

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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

class CreateAccount : ComponentActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.create_account)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val goBackLink = findViewById<TextView>(R.id.goBackLink)

        goBackLink.setOnClickListener {
            val intent = Intent(this, Welcome::class.java)
            startActivity(intent)
            finish()
        }

        val btnCreateAccount = findViewById<TextView>(R.id.btnCreateAccount)

        val inpName = findViewById<EditText>(R.id.inpName)
        val inpEmail = findViewById<EditText>(R.id.inpMail)
        val inpPass = findViewById<EditText>(R.id.inpPass)
        val inpRepass = findViewById<EditText>(R.id.inpRepass)

        btnCreateAccount.setOnClickListener {
            if(inpName.text.isEmpty()){
                inpEmail.setText("")
                inpPass.setText("")
                inpRepass.setText("")
                inpName.setText("")
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
                    inpEmail.setText("")
                    inpPass.setText("")
                    inpRepass.setText("")
                    inpName.setText("")
                    AlertDialog.Builder(this)
                        .setTitle("Erro")
                        .setMessage("Por favor, coloque o seu email!")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }else{
                    if(inpPass.text.length < 8){
                        inpEmail.setText("")
                        inpPass.setText("")
                        inpRepass.setText("")
                        inpName.setText("")
                        AlertDialog.Builder(this)
                            .setTitle("Erro")
                            .setMessage("Por favor, crie uma password com pelo menos 8 caracteres!")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }else{
                        if(inpPass.text.toString() != inpRepass.text.toString()){
                            AlertDialog.Builder(this)
                                .setTitle("Erro")
                                .setMessage("As passwords não correspondem!")
                                .setPositiveButton("OK") { dialog, _ ->
                                    inpEmail.setText("")
                                    inpPass.setText("")
                                    inpRepass.setText("")
                                    inpName.setText("")
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
                                "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Public/CreateAccount",
                                Response.Listener { response ->
                                    alertDialog.dismiss()
                                    try {
                                        val jsonObject = JSONObject(response)
                                            val status = jsonObject.getString("IsSuccess").toBoolean()
                                            if(status){
                                                AlertDialog.Builder(this)
                                                    .setTitle("Sucesso")
                                                    .setMessage("Bem-vindo ${inpName.text}, a tua conta foi criada com sucesso!")
                                                    .setPositiveButton("OK") { dialog, _ ->
                                                        dialog.dismiss()
                                                        val intent = Intent(this, Welcome::class.java)
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                    .create()
                                                    .show()
                                            }else{
                                                inpEmail.setText("")
                                                inpPass.setText("")
                                                inpRepass.setText("")
                                                inpName.setText("")
                                                AlertDialog.Builder(this)
                                                    .setTitle("Ocorreu um erro")
                                                    .setMessage(jsonObject.getString("Message"))
                                                    .setPositiveButton("OK") { dialog, _ ->
                                                        dialog.dismiss()
                                                        val intent = Intent(this, Welcome::class.java)
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                    .create()
                                                    .show()
                                            }
                                    } catch (e: JSONException) {
                                        inpEmail.setText("")
                                        inpPass.setText("")
                                        inpRepass.setText("")
                                        inpName.setText("")
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
                                            inpRepass.setText("")
                                            inpName.setText("")
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
                                        inpRepass.setText("")
                                        inpName.setText("")
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
                                    body.put("Password", inpPass.text.toString())
                                    return body.toString().toByteArray(Charsets.UTF_8)
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
        }
    }
}