package com.quiz.pride.ui.result

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.quiz.domain.App
import com.quiz.pride.R
import com.quiz.pride.common.inflate
import com.quiz.pride.utils.glideLoadBase64
import com.quiz.pride.utils.glideLoadURL
import com.quiz.pride.utils.setSafeOnClickListener

class AppListAdapter(private var context: Context,
                     var appList: MutableList<App>,
                     private val clickListener: (String) -> Unit,
) : RecyclerView.Adapter<AppListAdapter.AppListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppListViewHolder {
        val view = parent.inflate(R.layout.item_app, false)
        return AppListViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppListViewHolder, position: Int) {
        val app = appList[position]
        holder.appName.text = if(context.getString(R.string.locale) == "es") app.localeName?.ES!! else app.localeName?.EN!!
        glideLoadURL(context,  app.image, holder.appImage)
        holder.itemContainer.setSafeOnClickListener { clickListener(app.url!!) }
    }

    override fun getItemCount(): Int {
        return appList.size
    }

    fun getItem(position: Int): App {
        return appList[position]
    }

    class AppListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var appName: TextView = view.findViewById(R.id.appName)
        var appImage: ImageView = view.findViewById(R.id.appImage)
        var itemContainer: CardView = view.findViewById(R.id.itemContainer)
    }
}