package pt.spacelabs.experience.learnify

import pt.spacelabs.experience.learnify.Entitys.ClassDetailInfo
import pt.spacelabs.experience.learnify.Entitys.Course
import CourseAdapter
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
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Dashboard : ComponentActivity() {
    private var lastSelectedTabId: Int = R.id.nav_courses
    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.dashboard)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        registerNetworkCallback()

        val nameTxt = findViewById<TextView>(R.id.txtUserName)
        val txtNotFound = findViewById<TextView>(R.id.txtNotFound)
        val btnReadQrCode = findViewById<Button>(R.id.btnReadQrCode)

        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.loading, null)
        dialogBuilder.setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.show()

        val stringRequest = object : StringRequest(
            Method.POST,
            "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Private/GetUserData",
            Response.Listener { response ->
                alertDialog.dismiss()
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.has("Name")) {
                        val name = jsonObject.getString("Name")
                        val email = jsonObject.getString("Email")
                        DBHelper(this).createConfig("username", name)
                        DBHelper(this).createConfig("email", email)

                        nameTxt.text = name
                    }
                } catch (e: JSONException) {
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
                        AlertDialog.Builder(this)
                            .setTitle("Ocorreu um erro")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .create()
                            .show()
                    }
                } catch (e: Exception) {
                    nameTxt.text = DBHelper(this).getConfig("username")
                }
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val auth = "Bearer " + DBHelper(this@Dashboard).getConfig("auth")
                headers["Authorization"] = auth
                return headers
            }

            override fun getBody(): ByteArray {
                return "{studentId: 0}".toByteArray()
            }
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
            2000,
            2,
            1.0f
        )

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        var courseList = mutableListOf<Course>()

        alertDialog.show()
        val requestCourses = object : StringRequest(
            Method.POST,
            "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Private/GetCourses",
            Response.Listener { response ->
                alertDialog.dismiss()
                try {
                    val jsonArray = JSONArray(response)

                    for (index in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(index)
                        val coursesObject = jsonObject.getJSONObject("Courses")

                        if(coursesObject.getBoolean("IsEnable")){
                            val course = Course(
                                title = coursesObject.getString("Name"),
                                description = coursesObject.getString("Description"),
                                poster = coursesObject.getString("ImagePath"),
                                Id = coursesObject.getString("Id"),
                                FriendlyName = coursesObject.getString("FriendlyName"),
                                UUID = coursesObject.getString("UUID"),
                                isEnable = coursesObject.getBoolean("IsEnable")
                            )
                            courseList.add(course)
                        }
                    }

                    val adapter = CourseAdapter(courseList)
                    recyclerView.adapter = adapter
                } catch (e: JSONException) {
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
                        AlertDialog.Builder(this)
                            .setTitle("Ocorreu um erro")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .create()
                            .show()
                    }
                } catch (e: Exception) {
                    if(DBHelper(this).getCourses().isEmpty()){
                        txtNotFound.visibility = View.VISIBLE
                        btnReadQrCode.visibility = View.GONE
                    }else{
                        courseList = DBHelper(this).getCourses().toMutableList()
                        val adapter = CourseAdapter(courseList)
                        recyclerView.adapter = adapter
                    }
                }
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val auth = "Bearer " + DBHelper(this@Dashboard).getConfig("auth")
                headers["Authorization"] = auth
                return headers
            }

            override fun getBody(): ByteArray {
                return "{studentId: 0}".toByteArray()
            }
        }

        requestQueue.add(stringRequest)
        requestQueue.add(requestCourses)

        btnReadQrCode.setOnClickListener {
            val intent = Intent(this, QRCode::class.java)
            startActivity(intent)
            finish()
        }

        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = lastSelectedTabId

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.nav_courses -> {
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
                        bottomNavigationView.selectedItemId = R.id.nav_courses
                        dialog.dismiss()
                    }

                    builder.create().show()
                    true
                }

                else -> false
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
                    updateProfile()
                    updateCourses()
                    updateClasses()
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
        updateProfile()
        updateCourses()
        updateClasses()
    }

    fun updateProfile() {
        if (DBHelper(this@Dashboard).getConfig("haveToUpdateProfile") == "yes") {
            val requestQueue: RequestQueue = Volley.newRequestQueue(this@Dashboard)

            val stringRequest = object : StringRequest(
                Method.POST,
                "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Private/UpdateProfile",
                Response.Listener { response ->
                    try {
                        val jsonObject = JSONObject(response)
                        val status = jsonObject.getString("IsSuccess").toBoolean()
                        if (!status) {
                            AlertDialog.Builder(this@Dashboard)
                                .setTitle("Ocorreu um erro")
                                .setMessage(jsonObject.getString("Message"))
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()
                        } else {
                            DBHelper(this@Dashboard).clearConfig("haveToUpdateProfile")
                        }
                    } catch (e: JSONException) {
                    }
                },
                Response.ErrorListener { }
            ) {
                override fun getBody(): ByteArray {
                    val body = JSONObject()
                    body.put("Name", DBHelper(this@Dashboard).getConfig("username"))
                    body.put("Email", DBHelper(this@Dashboard).getConfig("email"))
                    return body.toString().toByteArray(Charsets.UTF_8)
                }

                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    val auth = "Bearer " + DBHelper(this@Dashboard).getConfig("auth")
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

    private fun updateCourses(){
        val curses: List<Course> = DBHelper(this).getCourses()

        for (index in curses.indices) {
            val requestQueue: RequestQueue = Volley.newRequestQueue(this)

            val requestCourse = object : StringRequest(
                Method.POST,
                "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Private/GetCourseByFriendlyName",
                Response.Listener { response ->
                    try {
                        val jsonArray = JSONArray(response)
                        val jsonObject = jsonArray.getJSONObject(0)
                        val coursesObject = jsonObject.getJSONObject("Courses")
                        if(curses[index].UUID !== coursesObject.getString("UUID")){
                            DBHelper(this).updateCourse(coursesObject.getString("Id"),coursesObject.getString("Name"),coursesObject.getString("Description"),coursesObject.getString("FriendlyName"),coursesObject.getString("ImagePath"),coursesObject.getString("UUID"),coursesObject.getBoolean("IsEnable"))
                        }
                    } catch (e: JSONException) {
                        AlertDialog.Builder(this)
                            .setTitle("Falha de ligação")
                            .setMessage("Ocorreu um erro com a resposta do servidor!")
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .create()
                            .show()
                    }
                },
                Response.ErrorListener { }
            ) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    val auth = "Bearer " + DBHelper(this@Dashboard).getConfig("auth")
                    headers["Authorization"] = auth
                    return headers
                }

                override fun getBody(): ByteArray {
                    val body = JSONObject()
                    body.put("CourseSmallName", curses[index].FriendlyName)
                    return body.toString().toByteArray(Charsets.UTF_8)
                }
            }

            requestQueue.add(requestCourse)
        }
    }

    private fun updateClasses(){
        val classes: List<ClassDetailInfo> = DBHelper(this).getClasses()

        for (index in classes.indices) {
            val requestQueue: RequestQueue = Volley.newRequestQueue(this)

            val requestCourse = object : StringRequest(
                Method.POST,
                "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Private/GetClassesByCourseId",
                Response.Listener { response ->
                    try {
                        val jsonArray = JSONArray(response)
                        val jsonObject = jsonArray.getJSONObject(0)
                        val coursesObject = jsonObject.getJSONObject("Classes")
                        if(classes[index].UUID !== coursesObject.getString("UUID")){
                            DBHelper(this).updateClass(coursesObject.getString("Id"),coursesObject.getString("CourseId"),coursesObject.getString("Name"),coursesObject.getString("Description"),coursesObject.getString("Path"),coursesObject.getString("ImagePath"),coursesObject.getString("UUID"),coursesObject.getBoolean("IsEnable"))
                        }
                    } catch (e: JSONException) {
                        AlertDialog.Builder(this)
                            .setTitle("Falha de ligação")
                            .setMessage("Ocorreu um erro com a resposta do servidor!")
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .create()
                            .show()
                    }
                },
                Response.ErrorListener { }
            ) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    val auth = "Bearer " + DBHelper(this@Dashboard).getConfig("auth")
                    headers["Authorization"] = auth
                    return headers
                }

                override fun getBody(): ByteArray {
                    val body = JSONObject()
                    body.put("CourseId", classes[index].CourseId)
                    return body.toString().toByteArray(Charsets.UTF_8)
                }
            }

            requestQueue.add(requestCourse)
        }
    }
}