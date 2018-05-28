package com.lingsatuo.thieves

import android.content.Intent
import android.media.MediaPlayer
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.lingsatuo.adapter.MPlaylistActivityRvAdapter
import com.lingsatuo.getqqmusic.*
import com.lingsatuo.service.MusicService
import com.lingsatuo.widget.XTextView
import com.lingsatuo.widget.XWebView

class PlaylistActivityInitView(private var playlistActivity: PlaylistActivity) {
    private val adapter = MPlaylistActivityRvAdapter(playlistActivity)
    private lateinit var rv: RecyclerView
    private var l = ArrayList<MusicItem>()
    private val listener: (Throwable?, ArrayList<MusicItem>) -> Unit = { a, b ->
        run {
            if (a != null) {
                playlistActivity.findViewById<XTextView>(R.id.playlist_avtivity_loading_error).text = a.toString()
            }
            if (b.size < 1) return@run
            l = b
            playlistActivity.findViewById<LinearLayout>(R.id.play_list_loading).visibility = View.GONE
            rv.visibility = View.VISIBLE
            adapter.setData(b)
            adapter.notifyDataSetChanged()
        }
    }

    fun setView() {
        val webView = playlistActivity.findViewById<XWebView>(R.id.playlist_webview)
        val group = playlistActivity.intent.getSerializableExtra("GROUP") as MusicGroup
        rv = playlistActivity.findViewById(R.id.playlist_rv)
        playlistActivity.findViewById<XTextView>(R.id.title).text = group.title
        rv.layoutManager = StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)
        rv.adapter = adapter
        val head = LayoutInflater.from(playlistActivity).inflate(R.layout.playlist_rv_head, null, false)
        adapter.setOnItemClickListener { i, view ->
            val item = adapter.getItem(i)
            if (item.singmid == MusicService.instance?.item?.singmid) {
                val intent = Intent(playlistActivity, PlayerActivity::class.java)
                playlistActivity.startActivity(intent)
            } else {
                Controller.index = i
                Controller.list = l
                MusicService.instance?.start(item)
            }
        }
        Glide.with(playlistActivity)
                .load(group.icon)
                .asBitmap()
                .placeholder(R.mipmap.loading)
                .priority(Priority.HIGH)
                .into(head.findViewById(R.id.play_list_head))
        //  adapter.setHand(head)
        webView.addFinishListener {
            MusicItemGet(listener, webView.getSource()).start()
        }
        webView.setOnErrorListener { e ->
            playlistActivity.findViewById<XTextView>(R.id.playlist_avtivity_loading_error).text = e.toString()
        }
        webView.loadUrl(group.href)
    }
}