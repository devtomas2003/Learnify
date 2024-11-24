import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pt.spacelabs.experience.learnify.Dashboard
import pt.spacelabs.experience.learnify.Entitys.ClassDetailInfo
import pt.spacelabs.experience.learnify.Entitys.Playback
import pt.spacelabs.experience.learnify.Player
import pt.spacelabs.experience.learnify.R
import pt.spacelabs.experience.learnify.Utils.OfflinePlayback

class ClassDetailAdapter(private val classDetailList: List<ClassDetailInfo>) :
    RecyclerView.Adapter<ClassDetailAdapter.ClassDetailViewHolder>() {

    inner class ClassDetailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.txtClassName)
        val descriptionTextView: TextView = view.findViewById(R.id.txtClassDescription)
        val classImage: ImageView = view.findViewById(R.id.classImage)
        val btnDownload: ImageView = view.findViewById(R.id.btnDownload)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassDetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.class_detail_item, parent, false)
        return ClassDetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassDetailViewHolder, position: Int) {
        val classDetail = classDetailList[position]
        holder.titleTextView.text = classDetail.Name
        holder.descriptionTextView.text = classDetail.Description

        Glide
            .with(holder.itemView.context)
            .load("https://vis-ipv-cda.epictv.spacelabs.pt/public/" + classDetail.ImagePath)
            .centerCrop()
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.classImage)

        if(DBHelper(holder.itemView.context).getClassById(classDetail.Id) != null){
            holder.btnDownload.setImageResource(R.drawable.close_dark)
        }

        holder.classImage.setOnClickListener {
            val intent = Intent(holder.itemView.context, Player::class.java)
            intent.putExtra("manifestId", classDetail.Path)
            intent.putExtra("classId", classDetail.Id)
            holder.itemView.context.startActivity(intent)
        }

        holder.btnDownload.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                if (DBHelper(holder.itemView.context).getClassById(classDetail.Id) == null) {
                    AlertDialog.Builder(holder.itemView.context)
                        .setTitle("Confirmar ação")
                        .setMessage("Pretende realmente descarregar esta aula?")
                        .setPositiveButton("Sim"){ _, _ ->

                            DBHelper(holder.itemView.context).createClass(
                                classDetail.Id,
                                classDetail.CourseId,
                                classDetail.Name,
                                classDetail.Description,
                                classDetail.Path,
                                classDetail.ImagePath,
                                classDetail.UUID,
                                classDetail.isEnable
                            )
                            if(DBHelper(holder.itemView.context).getCourseById(classDetail.CourseId) == null){
                                val requestQueue: RequestQueue = Volley.newRequestQueue(holder.itemView.context)

                                val requestCourse = object : StringRequest(
                                    Method.POST,
                                    "https://personal-jw7ryxr1.outsystemscloud.com/Learnify_BL/rest/v1Private/GetCourseById",
                                    Response.Listener { response ->
                                        try {
                                            val jsonArray = JSONArray(response)
                                            val jsonObject = jsonArray.getJSONObject(0)
                                            val coursesObject = jsonObject.getJSONObject("Courses")

                                            DBHelper(holder.itemView.context).createCourse(coursesObject.getString("Id"),coursesObject.getString("Name"),coursesObject.getString("Description"),coursesObject.getString("FriendlyName"),coursesObject.getString("ImagePath"),coursesObject.getString("UUID"),coursesObject.getBoolean("IsEnable"))
                                        } catch (e: JSONException) {
                                            AlertDialog.Builder(holder.itemView.context)
                                                .setTitle("Falha de ligação")
                                                .setMessage("Ocorreu um erro com a resposta do servidor!")
                                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                                .create()
                                                .show()
                                        }
                                    },
                                    Response.ErrorListener { error ->
                                        try {
                                            val errorResponse = String(error.networkResponse.data, Charsets.UTF_8)
                                            val errorObject = JSONObject(errorResponse)
                                            if (errorObject.has("Message")) {
                                                val errorMessage = errorObject.getString("Errors")
                                                AlertDialog.Builder(holder.itemView.context)
                                                    .setTitle("Ocorreu um erro")
                                                    .setMessage(errorMessage)
                                                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                                    .create()
                                                    .show()
                                            }
                                        } catch (e: Exception) {
                                            AlertDialog.Builder(holder.itemView.context)
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
                                        val auth = "Bearer " + DBHelper(holder.itemView.context).getConfig("auth")
                                        headers["Authorization"] = auth
                                        return headers
                                    }

                                    override fun getBody(): ByteArray {
                                        val body = JSONObject()
                                        body.put("CourseId", classDetail.CourseId)
                                        return body.toString().toByteArray(Charsets.UTF_8)
                                    }
                                }

                                requestQueue.add(requestCourse)
                            }
                            val downloadIntent = Intent(holder.itemView.context, OfflinePlayback::class.java)
                            downloadIntent.putExtra("classId", classDetail.Id)
                            downloadIntent.putExtra("manifestName", classDetail.Path)
                            downloadIntent.putExtra("className", classDetail.Name)
                            holder.itemView.context.startForegroundService(downloadIntent)
                            holder.btnDownload.setImageResource(R.drawable.close_dark)
                        }
                        .setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
                } else {
                    AlertDialog.Builder(holder.itemView.context)
                        .setTitle("Confirmar ação")
                        .setMessage("Pretende realmente apagar esta aula?")
                        .setPositiveButton("Sim"){ _, _ ->
                            DBHelper(holder.itemView.context).deleteClass(classDetail.Id)
                            holder.btnDownload.setImageResource(R.drawable.download)
                            val cdi: List<ClassDetailInfo>? = DBHelper(holder.itemView.context).getClassesByCourse(classDetail.CourseId)

                            val listPlaybacks: List<Playback>? = DBHelper(holder.itemView.context).getChunksByClassID(classDetail.Id)

                            listPlaybacks?.forEach { playback ->
                                holder.itemView.context.deleteFile(playback.chunk)
                            }

                            DBHelper(holder.itemView.context).deleteChunks(classDetail.Id)

                            if (cdi != null) {
                                if(cdi.isEmpty()){
                                    DBHelper(holder.itemView.context).deleteCourse(classDetail.CourseId)
                                    if(!isInternetAvailable(holder.itemView.context) && holder.itemView.context is Activity){
                                        val intent = Intent(holder.itemView.context, Dashboard::class.java)
                                        holder.itemView.context.startActivity(intent)
                                        (holder.itemView.context as Activity).finish()
                                    }
                                }
                            }

                            if(!isInternetAvailable(holder.itemView.context)){
                                (classDetailList as MutableList<ClassDetailInfo>).removeAt(currentPosition)
                                notifyItemRemoved(currentPosition)
                                notifyItemRangeChanged(currentPosition, itemCount - currentPosition)
                            }
                        }
                        .setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
                }
            }
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun getItemCount(): Int = classDetailList.size
}