package com.quiz.pride.ui.ranking

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quiz.domain.User
import com.quiz.pride.R
import com.quiz.pride.common.inflate
import com.quiz.pride.utils.getRelationTime
import com.quiz.pride.utils.glideCircleLoadBase64
import kotlinx.android.synthetic.main.item_ranking_user.view.*

class RankingListAdapter(
    val context: Context,
    var rankingList: MutableList<User>) : RecyclerView.Adapter<RankingListAdapter.RankingListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingListViewHolder {
        val view = parent.inflate(R.layout.item_ranking_user, false)
        return RankingListViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingListViewHolder, position: Int) {
        val user = rankingList[position]

        when (position) {
            0 -> {
                holder.firstPositionImage.visibility = View.VISIBLE
                holder.positionText.visibility = View.GONE
                holder.firstPositionImage.setImageResource(R.drawable.medal_gold)
            }
            1 -> {
                holder.firstPositionImage.visibility = View.VISIBLE
                holder.positionText.visibility = View.GONE
                holder.firstPositionImage.setImageResource(R.drawable.medal_silver)
            }
            2 -> {
                holder.firstPositionImage.visibility = View.VISIBLE
                holder.positionText.visibility = View.GONE
                holder.firstPositionImage.setImageResource(R.drawable.medal_bronze)
            }
            else -> holder.positionText.text = (position + 1).toString()
        }

        holder.userName.text = if(user.name.isNullOrEmpty()) context.getString(R.string.anonymous) else user.name
        glideCircleLoadBase64(context,  user.userImage, holder.userImage)
        holder.timeText.text = getRelationTime(user.timestamp!!)
        holder.userPoints.text = user.points
    }

    override fun getItemCount(): Int {
        return rankingList.size
    }

    fun getItem(position: Int): User {
        return rankingList[position]
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class RankingListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var positionText: TextView = view.findViewById(R.id.positionText)
        var firstPositionImage: ImageView = view.findViewById(R.id.firstPositionImage)
        var userName: TextView = view.findViewById(R.id.nameText)
        var userImage: ImageView = view.findViewById(R.id.userImage)
        var timeText: TextView = view.findViewById(R.id.timeText)
        var userPoints: TextView = view.findViewById(R.id.userPoints)
    }
}