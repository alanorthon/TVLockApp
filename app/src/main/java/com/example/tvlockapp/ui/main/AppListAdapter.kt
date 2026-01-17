package com.example.tvlockapp.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tvlockapp.R

class AppListAdapter(
    private val appList: List<AppInfo>,
    private val onItemClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
        val appName: TextView = itemView.findViewById(R.id.app_name)
        val limitInfo: TextView = itemView.findViewById(R.id.limit_info)
        val usageInfo: TextView = itemView.findViewById(R.id.usage_info)
        val statusIndicator: View = itemView.findViewById(R.id.status_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = appList[position]
        
        holder.appIcon.setImageDrawable(appInfo.icon)
        holder.appName.text = appInfo.appName
        
        if (appInfo.hasLimit()) {
            val usageMinutes = appInfo.getDailyUsageInMinutes()
            val limitMinutes = appInfo.getDailyLimitInMinutes()
            val remainingMinutes = appInfo.getRemainingTimeInMinutes()

            holder.limitInfo.text = "Использовано: ${usageMinutes} мин"
            holder.usageInfo.text = "Лимит: ${limitMinutes} мин (осталось: ${remainingMinutes} мин)"
            
            // Set status indicator color
            if (appInfo.isLimitExceeded()) {
                holder.statusIndicator.setBackgroundColor(
                    holder.itemView.context.getColor(android.R.color.holo_red_dark)
                )
                holder.usageInfo.text = "Лимит исчерпан!"
            } else if (remainingMinutes <= 30) {
                holder.statusIndicator.setBackgroundColor(
                    holder.itemView.context.getColor(android.R.color.holo_orange_dark)
                )
            } else {
                holder.statusIndicator.setBackgroundColor(
                    holder.itemView.context.getColor(android.R.color.holo_green_dark)
                )
            }
        } else {
            val usageMinutes = appInfo.getDailyUsageInMinutes()
            holder.limitInfo.text = "Использовано: ${usageMinutes} мин"
            holder.usageInfo.text = "Нажмите для установки лимита"
            holder.statusIndicator.setBackgroundColor(
                holder.itemView.context.getColor(android.R.color.darker_gray)
            )
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(appInfo)
        }
        
        // Set focus handling for TV
        holder.itemView.isFocusable = true
        holder.itemView.isFocusableInTouchMode = true
    }

    override fun getItemCount(): Int = appList.size
}