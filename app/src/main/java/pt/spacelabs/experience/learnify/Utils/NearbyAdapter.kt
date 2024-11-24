package pt.spacelabs.experience.learnify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NearbyAdapter(
    private val nearbyNames: List<String>
) : RecyclerView.Adapter<NearbyAdapter.NearbyViewHolder>() {

    class NearbyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nearbyName: TextView = view.findViewById(R.id.lblNearbyName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearbyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nearby, parent, false)
        return NearbyViewHolder(view)
    }

    override fun onBindViewHolder(holder: NearbyViewHolder, position: Int) {
        holder.nearbyName.text = nearbyNames[position]
    }

    override fun getItemCount(): Int {
        return nearbyNames.size
    }
}