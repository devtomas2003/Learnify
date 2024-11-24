package pt.spacelabs.experience.learnify

import DBHelper
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class QRCode : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan a course poster")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        barcodeLauncher.launch(options)
    }

    private val barcodeLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        } else {
            val requestQueue: RequestQueue = Volley.newRequestQueue(this)

            val requestCourse = object : StringRequest(
                Method.POST,
                "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Private/GetCourseByFriendlyName",
                Response.Listener { response ->
                    try {
                        val jsonArray = JSONArray(response)
                        val jsonObject = jsonArray.getJSONObject(0)
                        val coursesObject = jsonObject.getJSONObject("Courses")
                        if(coursesObject.getBoolean("IsEnable")){
                            val intent = Intent(this, ClassDetail::class.java)
                            intent.putExtra("courseSmallName", result.contents)
                            startActivity(intent)
                            finish()
                        }else{
                            AlertDialog.Builder(this)
                                .setTitle("Ocorreu um erro")
                                .setMessage("Não foi encontrado nenhum curso com este QRCode!")
                                .setPositiveButton("OK") { dialog, _ ->
                                    val intent = Intent(this, Dashboard::class.java)
                                    startActivity(intent)
                                    finish()
                                    dialog.dismiss() }
                                .create()
                                .show()
                        }
                    } catch (e: JSONException) {
                        AlertDialog.Builder(this)
                            .setTitle("Falha de ligação")
                            .setMessage("Ocorreu um erro com a resposta do servidor!")
                            .setPositiveButton("OK") { dialog, _ ->
                                val intent = Intent(this, Dashboard::class.java)
                                startActivity(intent)
                                finish()
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }
                },
                Response.ErrorListener { error ->
                    try {
                        val errorResponse = String(error.networkResponse.data, Charsets.UTF_8)
                        val errorObject = JSONObject(errorResponse)
                        if (errorObject.has("Message")) {
                            val errorMessage = errorObject.getString("Message")
                            AlertDialog.Builder(this)
                                .setTitle("Ocorreu um erro")
                                .setMessage(errorMessage)
                                .setPositiveButton("OK") { dialog, _ ->
                                    val intent = Intent(this, Dashboard::class.java)
                                    startActivity(intent)
                                    finish()
                                    dialog.dismiss() }
                                .create()
                                .show()
                        }
                    } catch (e: Exception) {
                        if(DBHelper(this).getCourseFriendlyName(result.contents) == null){
                            AlertDialog.Builder(this)
                                .setTitle("Ocorreu um erro")
                                .setMessage("Não foi encontrado nenhum curso com este QRCode!")
                                .setPositiveButton("OK") { dialog, _ ->
                                    val intent = Intent(this, Dashboard::class.java)
                                    startActivity(intent)
                                    finish()
                                    dialog.dismiss() }
                                .create()
                                .show()
                        }else{
                            val intent = Intent(this, ClassDetail::class.java)
                            intent.putExtra("courseSmallName", result.contents)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            ) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    val auth = "Bearer " + DBHelper(this@QRCode).getConfig("auth")
                    headers["Authorization"] = auth
                    return headers
                }

                override fun getBody(): ByteArray {
                    val body = JSONObject()
                    body.put("CourseSmallName", result.contents)
                    return body.toString().toByteArray(Charsets.UTF_8)
                }
            }

            requestQueue.add(requestCourse)
        }
    }

}