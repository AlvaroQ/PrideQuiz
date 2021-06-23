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
import com.quiz.pride.utils.glideLoadURL

class InfoListAdapter(
    val context: Context,
    var infoList: MutableList<Pride>) : RecyclerView.Adapter<InfoListAdapter.InfoListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoListViewHolder {
        val view = parent.inflate(R.layout.item_info, false)
        return InfoListViewHolder(view)
    }

    override fun onBindViewHolder(holder: InfoListViewHolder, position: Int) {
        val pride = infoList[position]

        glideLoadURL(context,  pride.flag, holder.flagImage)

        val nameLocalize = when {
            context.getString(R.string.locale) == "es" -> pride.name?.ES
            context.getString(R.string.locale) == "fr" -> pride.name?.FR
            context.getString(R.string.locale) == "pt" -> pride.name?.PT
            context.getString(R.string.locale) == "de" -> pride.name?.DE
            context.getString(R.string.locale) == "it" -> pride.name?.IT
            else -> pride.name?.EN
        }
        holder.nameText.text = nameLocalize

        val descriptionLocalize = when {
            context.getString(R.string.locale) == "es" -> pride.description?.ES
            context.getString(R.string.locale) == "fr" -> pride.description?.FR
            context.getString(R.string.locale) == "pt" -> pride.description?.PT
            context.getString(R.string.locale) == "de" -> pride.description?.DE
            context.getString(R.string.locale) == "it" -> pride.description?.IT
            else -> pride.name?.EN
        }
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