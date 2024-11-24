package pt.spacelabs.experience.learnify

import ClassDetailAdapter
import pt.spacelabs.experience.learnify.Entitys.ClassDetailInfo
import pt.spacelabs.experience.learnify.Entitys.Course
import DBHelper
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ClassDetail : ComponentActivity() {
    private var lastSelectedTabId: Int = R.id.nav_courses
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.classdetail)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions

        val txtTitle = findViewById<TextView>(R.id.txtTitle)
        val txtDescription = findViewById<TextView>(R.id.txtDescription)
        val imgBanner = findViewById<ImageView>(R.id.imgBanner)

        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.loading, null)
        dialogBuilder.setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.show()

        val requestCourse = object : StringRequest(
            Method.POST,
            "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Private/GetCourseByFriendlyName",
            Response.Listener { response ->
                alertDialog.dismiss()
                try {
                        val jsonArray = JSONArray(response)
                        val jsonObject = jsonArray.getJSONObject(0)
                        val coursesObject = jsonObject.getJSONObject("Courses")

                        txtTitle.setText(coursesObject.getString("Name"))
                        txtDescription.setText(coursesObject.getString("Description"))

                    Glide
                        .with(this)
                        .load("https://vis-ipv-cda.epictv.spacelabs.pt/public/" + coursesObject.getString("ImagePath"))
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(imgBanner)

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
                    val cs: Course? = DBHelper(this).getCourseFriendlyName(intent.getStringExtra("courseSmallName").toString())
                    txtTitle.setText(cs!!.title)
                    txtDescription.setText(cs!!.description)

                    Glide
                        .with(this)
                        .load("https://vis-ipv-cda.epictv.spacelabs.pt/public/" + cs!!.poster)
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(imgBanner)
                }
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val auth = "Bearer " + DBHelper(this@ClassDetail).getConfig("auth")
                headers["Authorization"] = auth
                return headers
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("CourseSmallName", intent.getStringExtra("courseSmallName"))
                return body.toString().toByteArray(Charsets.UTF_8)
            }
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        var classesList = mutableListOf<ClassDetailInfo>()

        alertDialog.show()

        val requestClasses = object : StringRequest(
            Method.POST,
            "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Private/GetClassesByCourseName",
            Response.Listener { response ->
                alertDialog.dismiss()
                try {
                    val jsonArray = JSONArray(response)

                    for (index in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(index)
                        val classesObject = jsonObject.getJSONObject("Classes")

                        if(classesObject.getBoolean("IsEnable")){
                            val classDetail = ClassDetailInfo(
                                Id = classesObject.getString("Id"),
                                CourseId = classesObject.getString("CourseId"),
                                Name = classesObject.getString("Name"),
                                Description = classesObject.getString("Description"),
                                Path = classesObject.getString("Path"),
                                ImagePath = classesObject.getString("ImagePath"),
                                UUID = classesObject.getString("UUID"),
                                isEnable = classesObject.getBoolean("IsEnable")
                            )

                            classesList.add(classDetail)
                        }
                    }

                    val adapter = ClassDetailAdapter(classesList)
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
                    val course: Course? = intent.getStringExtra("courseSmallName")
                        ?.let { DBHelper(this).getCourseFriendlyName(it) }
                    if (course != null) {
                        classesList = DBHelper(this).getClassesByCourse(course.Id) as MutableList<ClassDetailInfo>
                        val adapter = ClassDetailAdapter(classesList)
                        recyclerView.adapter = adapter
                    }
                }
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val auth = "Bearer " + DBHelper(this@ClassDetail).getConfig("auth")
                headers["Authorization"] = auth
                return headers
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("CourseSmallName", intent.getStringExtra("courseSmallName"))
                return body.toString().toByteArray(Charsets.UTF_8)
            }
        }

        requestQueue.add(requestCourse)
        requestQueue.add(requestClasses)

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
}