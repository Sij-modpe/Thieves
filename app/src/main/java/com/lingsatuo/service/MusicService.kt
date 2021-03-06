package com.lingsatuo.service

import android.app.*
import android.os.*
import android.content.*
import android.media.*
import com.lingsatuo.getqqmusic.GetMusicAbsPath
import com.lingsatuo.getqqmusic.GetMusicFileName
import com.lingsatuo.getqqmusic.MusicItem
import com.lingsatuo.getqqmusic.RunOnUiThread
import com.lingsatuo.thieves.Controller
import com.lingsatuo.utils.FileUtils
import com.lingsatuo.utils.MNotificationManager
import java.io.File
import java.util.ArrayList

class MusicService : Service() {
    val listeners = ArrayList<(MusicItem) -> Unit>()
    private var am: AudioManager? = null
    var item = MusicItem()
    var path: String? = null
    private val bufferingupdate: (MediaPlayer, Int) -> Unit = { m, p ->
        if (p > 30 && !m.isPlaying && canplay) {
            play()
            canplay = false
        }
    }
    private var afChangeListener: AudioManager.OnAudioFocusChangeListener? = null


    companion object {
        var instance: MusicService? = null
        val player: MediaPlayer = MediaPlayer()
        var canplay = true
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        addListener()
        MNotificationManager.show(this)
        Controller.addBufferingUpdateListener(bufferingupdate)
    }

    fun addListener(lis: (MusicItem) -> Unit) {
        if (!listeners.contains(lis)) {
            listeners.add(lis)
        }
    }

    fun removeListener(lis: (MusicItem) -> Unit) {
        if (listeners.contains(lis)) {
            listeners.remove(lis)
        }
    }

    fun play(playing: Boolean) {
        play()
    }

    fun play() {
        val play = am!!.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        if (play == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            RunOnUiThread {
                Controller.listener.invoke(Controller.Type.PLAY)
            }
        }
    }

    fun seekTo(duration: Int) {
        if (canPlay()) {
            player.seekTo(duration)
        }
    }

    fun start() {
        am!!.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        player.start()
    }

    private fun start(path: String) {
        this.path = path
        Thread(Runnable {
            player.reset()
            try {
                canplay = true
                val tag = FileUtils.musicFileExists(item)
                if (tag == "")
                    player.setDataSource(path)
                else player.setDataSource(tag)
                player.prepare()
                if (this@MusicService.item.isloca || tag != "") {
                    bufferingupdate.invoke(player, 100)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }).start()
    }

    fun start(item: MusicItem) {
        this.item = item
        GetMusicAbsPath(item, GetMusicFileName.Quality.MP3, { path ->
            start(path)
            for (lis in 0 until listeners.size) {
                if (lis >= listeners.size) break
                listeners[lis].invoke(item)
            }
        }).start()
    }

    fun canPlay(): Boolean {
        return player.duration > 100
    }

    fun stop() {
        player.stop()
        am!!.abandonAudioFocus(afChangeListener)
    }

    fun pause(never: Boolean) {
        pause()
    }

    fun pause() {
        Controller.listener.invoke(Controller.Type.PAUSE)
        am!!.abandonAudioFocus(afChangeListener)
    }

    override fun onBind(p1: Intent): IBinder? {
        // TODO: Implement this method
        return null
    }

    fun addListener() {
        afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                if (player.isPlaying) {
                    pause()
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                if (player == null) {

                } else if (!player.isPlaying) {
                    if (!canplay) return@OnAudioFocusChangeListener
                    // play()
                }
                // Resume playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                if (player.isPlaying) {
                    pause()
                }
                //am.abandonAudioFocus(this);
                // Stop playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                if (!player.isPlaying) {
                    if (!canplay) return@OnAudioFocusChangeListener
                    // play()
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                if (player.isPlaying) {
                    pause()
                }
            }
        }
    }
}