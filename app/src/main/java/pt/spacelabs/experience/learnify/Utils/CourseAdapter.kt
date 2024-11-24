import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.spacelabs.experience.learnify.ClassDetail
import pt.spacelabs.experience.learnify.Entitys.Course
import pt.spacelabs.experience.learnify.R

class CourseAdapter(private val courseList: List<Course>) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.textView4)
        val descriptionTextView: TextView = view.findViewById(R.id.textView5)
        val imgBanner: ImageView = view.findViewById(R.id.imgBanner)
        val boxCourse: CardView = view.findViewById(R.id.boxCourse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.course_item, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courseList[position]
        holder.titleTextView.text = course.title
        holder.descriptionTextView.text = course.description

        holder.boxCourse.setOnClickListener {
            val intent = Intent(holder.itemView.context, ClassDetail::class.java)
            intent.putExtra("courseSmallName", course.FriendlyName)
            holder.itemView.context.startActivity(intent)
        }

        Glide
            .with(holder.itemView.context)
            .load("https://vis-ipv-cda.epictv.spacelabs.pt/public/" + course.poster)
            .centerCrop()
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imgBanner)
    }

    override fun getItemCount(): Int = courseList.size
}