package com.quiz.pride.ui.info

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quiz.domain.Pride
import com.quiz.pride.R
import com.quiz.pride.common.inflate
import com.quiz.pride.utils.glideLoadBase64

class InfoListAdapter(
    val context: Context,
    var infoList: MutableList<Pride>) : RecyclerView.Adapter<InfoListAdapter.InfoListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoListViewHolder {
        val view = parent.inflate(R.layout.item_info, false)
        return InfoListViewHolder(view)
    }

    override fun onBindViewHolder(holder: InfoListViewHolder, position: Int) {
        val pride = infoList[position]

        glideLoadBase64(context,  pride.flag, holder.flagImage)

        val nameLocalize = if(context.getString(R.string.locale) == "en") pride.name?.EN else pride.name?.ES
        holder.nameText.text = nameLocalize

        val descriptionLocalize = if(context.getString(R.string.locale) == "en") pride.description?.EN else pride.description?.ES
        holder.descriptionText.text = descriptionLocalize
    }

    override fun getItemCount(): Int {
        return infoList.size
    }

    fun getItem(position: Int): Pride {
        return infoList[position]
    }

    class InfoListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var nameText: TextView = view.findViewById(R.id.nameText)
        var flagImage: ImageView = view.findViewById(R.id.flagImage)
        var descriptionText: TextView = view.findViewById(R.id.descriptionText)
    }
}