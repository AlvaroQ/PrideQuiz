package com.quiz.pride.ui.moreApps

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quiz.domain.App
import com.quiz.pride.R
import com.quiz.pride.common.inflate
import com.quiz.pride.utils.glideLoadURL

class MoreAppsListAdapter(
    val context: Context,
    var rankingList: MutableList<App>) : RecyclerView.Adapter<MoreAppsListAdapter.RankingListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingListViewHolder {
        val view = parent.inflate(R.layout.item_app_list, false)
        return RankingListViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingListViewHolder, position: Int) {
        val app = rankingList[position]
        holder.applicationNameText.text = if(context.getString(R.string.locale) == "en") app.localeName?.EN!! else app.localeName?.ES!!
        holder.applicationDescriptionText.text = if(context.getString(R.string.locale) == "en") app.localeDescription?.EN!! else app.localeDescription?.ES!!
        glideLoadURL(context,  app.image, holder.applicationImage)
    }

    override fun getItemCount(): Int {
        return rankingList.size
    }

    fun getItem(position: Int): App {
        return rankingList[position]
    }

    class RankingListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var applicationNameText: TextView = view.findViewById(R.id.applicationNameText)
        var applicationDescriptionText: TextView = view.findViewById(R.id.applicationDescriptionText)
        var applicationImage: ImageView = view.findViewById(R.id.applicationImage)
    }
}