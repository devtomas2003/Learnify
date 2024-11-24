package pt.spacelabs.experience.learnify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PermissionAdapter(
    private val permissionDescriptions: List<String>
) : RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder>() {

    class PermissionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description: TextView = view.findViewById(R.id.tvPermissionDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_permission, parent, false)
        return PermissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        holder.description.text = permissionDescriptions[position]
    }

    override fun getItemCount(): Int {
        return permissionDescriptions.size
    }
}