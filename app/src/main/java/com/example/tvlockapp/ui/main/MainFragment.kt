package com.example.tvlockapp.ui.main

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.content.Intent
import android.view.ViewGroup
import android.widget.Button
import com.example.tvlockapp.ui.settings.SettingsActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tvlockapp.R
import com.example.tvlockapp.data.database.AppDatabase
import com.example.tvlockapp.data.model.AppLimit
import com.example.tvlockapp.data.repository.UsageStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppListAdapter
    private lateinit var database: AppDatabase
    private lateinit var usageStatsRepository: UsageStatsRepository
    private val appList = mutableListOf<AppInfo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onResume() {
        super.onResume()
        loadInstalledApps()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())
        usageStatsRepository = UsageStatsRepository(requireContext())

        recyclerView = view.findViewById(R.id.apps_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(context, 3)

        adapter = AppListAdapter(appList) { appInfo ->
            showLimitDialog(appInfo)
        }
        recyclerView.adapter = adapter

        loadInstalledApps()

        view.findViewById<Button>(R.id.settings_button).setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadInstalledApps() {
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                getInstalledApps()
            }

            appList.clear()
            appList.addAll(apps)
            adapter.notifyDataSetChanged()
        }
    }

    private suspend fun getInstalledApps(): List<AppInfo> {
        val packageManager = requireContext().packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val prefs = requireContext().getSharedPreferences("com.example.tvlockapp_preferences", Context.MODE_PRIVATE)
        val showSystemApps = prefs.getBoolean("show_system_apps", false)
        val hideOwnApp = prefs.getBoolean("hide_app_icon", false)

        val usageMap = usageStatsRepository.getUsageMapForToday()
        val myPackageName = requireContext().packageName

        val result = installedApps.mapNotNull { appInfo ->
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (isSystemApp && !showSystemApps) return@mapNotNull null

            val isOwnApp = appInfo.packageName == myPackageName
            if (isOwnApp && hideOwnApp) return@mapNotNull null

            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val icon = packageManager.getApplicationIcon(appInfo)
            val appLimit = database.appLimitDao().getAppLimit(appInfo.packageName)
            val currentLimit = appLimit?.dailyUsageLimit ?: 0L
            val dailyUsage = usageMap[appInfo.packageName] ?: 0

            AppInfo(
                packageName = appInfo.packageName,
                appName = appName,
                icon = icon,
                dailyLimit = currentLimit,
                dailyUsage = dailyUsage
            )
        }

        return result.sortedWith(compareByDescending<AppInfo> { it.dailyLimit > 0 }.thenBy { it.appName })
    }

    private fun showLimitDialog(appInfo: AppInfo) {
        val dialog = LimitSetDialog.newInstance(appInfo.packageName, appInfo.appName, appInfo.dailyLimit)
        dialog.setOnLimitSetListener { packageName, limitInMinutes ->
            setAppLimit(packageName, limitInMinutes)
        }
        dialog.show(parentFragmentManager, "limit_dialog")
    }

    private fun setAppLimit(packageName: String, limitInMinutes: Long) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val limitInMillis = limitInMinutes * 60 * 1000
                val existingAppLimit = database.appLimitDao().getAppLimit(packageName)

                if (existingAppLimit != null) {
                    database.appLimitDao().update(
                        existingAppLimit.copy(dailyUsageLimit = limitInMillis)
                    )
                } else {
                    database.appLimitDao().insert(
                        AppLimit(
                            packageName = packageName,
                            dailyUsageLimit = limitInMillis
                        )
                    )
                }
            }
            loadInstalledApps()
        }
    }
}