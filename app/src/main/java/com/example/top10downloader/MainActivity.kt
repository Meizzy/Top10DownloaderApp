package com.example.top10downloader

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.example.top10downloader.databinding.ActivityMainBinding
import com.google.android.material.switchmaterial.SwitchMaterial


class FeedEntry {
    var name: String = ""
    var artist: String = ""
    var releaseDate: String = ""
    var summary: String = ""
    var imageURL: String = ""

}

private const val TAG = "MainActivity"
private const val STATE_URL = "feedUrl"
private const val STATE_LIMIT = "feedLimit"
private const val MODE_QUERY = "ModeQuery"

class MainActivity : AppCompatActivity() {

    private var feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
    private var feedLimit = 10
    private lateinit var binding: ActivityMainBinding
    private val feedViewModel: FeedViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        val mode = PreferenceManager.getDefaultSharedPreferences(this)
        val defaultMode = mode.getInt(MODE_QUERY,1)
        AppCompatDelegate.setDefaultNightMode(defaultMode)


        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        Log.d(TAG, "onCreate called")

        val feedAdapter = FeedAdapter(this, R.layout.list_record, EMPTY_FEED_LIST)
        binding.xmlListView.adapter = feedAdapter

        if (savedInstanceState != null) {
            feedUrl = savedInstanceState.getString(STATE_URL).toString()
            feedLimit = savedInstanceState.getInt(STATE_LIMIT)
        }

        feedViewModel.feedEntries.observe(this
        ) { feedEntries -> feedAdapter.setFeedList(feedEntries ?: EMPTY_FEED_LIST) }
//        feedViewModel.feedEntries.observe(this,
//                Observer<List<FeedEntry>> { feedEntries -> feedAdapter.setFeedList(feedEntries!!) })


        feedViewModel.downloadUrl(feedUrl.format(feedLimit))
        Log.d(TAG, "onCreate: done")
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val mode = PreferenceManager.getDefaultSharedPreferences(this)
        val defaultMode = mode.getInt(MODE_QUERY,1)
        menuInflater.inflate(R.menu.feeds_menu, menu)

        if (feedLimit == 10) {
            menu?.findItem(R.id.mnu10)?.isChecked = true
        } else {
            menu?.findItem(R.id.mnu25)?.isChecked = true
        }
        if (menu != null) {
            val switchMaterial = menu.findItem(R.id.mnu_app_bar_switch)
                .actionView!!.findViewById(R.id.switch_btn) as SwitchMaterial
            if (defaultMode == AppCompatDelegate.MODE_NIGHT_YES){
                switchMaterial.isChecked = true
            } else{
                switchMaterial.isSelected = false
            }
            switchMaterial.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
//                    Log.d(TAG, "setting dark mode")
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    mode.edit().putInt(MODE_QUERY, AppCompatDelegate.MODE_NIGHT_YES).apply()
                } else {
//                    Log.d(TAG, "setting light mode")
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    mode.edit().putInt(MODE_QUERY, AppCompatDelegate.MODE_NIGHT_NO).apply()
                }

            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

//        Log.d(TAG,"onOptionsItemSelected clicked")

        when (item.itemId) {
            R.id.mnuFree ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
            R.id.mnuPaid ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml"
            R.id.mnuSongs ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"
            R.id.mnu10, R.id.mnu25 -> {
                if (!item.isChecked) {
                    item.isChecked = true
                    feedLimit = 35 - feedLimit
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} setting feedLimit to $feedLimit")
                } else {
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} feedLimit unchanged")
                }
            }
            R.id.mnuRefresh -> feedViewModel.invalidate()
            R.id.mnu_app_bar_switch ->
            {

            }
            else ->
                return super.onOptionsItemSelected(item)
        }
        feedViewModel.downloadUrl(feedUrl.format(feedLimit))
        return true
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_URL, feedUrl)
        outState.putInt(STATE_LIMIT, feedLimit)
    }
}
