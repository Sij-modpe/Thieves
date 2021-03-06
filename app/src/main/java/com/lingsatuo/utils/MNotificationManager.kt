package com.lingsatuo.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews
import android.widget.Toast
import com.lingsatuo.getqqmusic.MusicItem
import com.lingsatuo.getqqmusic.RunOnUiThread
import com.lingsatuo.receiver.NotifictionReceiver
import com.lingsatuo.service.MusicService
import com.lingsatuo.thieves.Controller
import com.lingsatuo.thieves.PlayerActivity
import com.lingsatuo.thieves.R
import java.io.File

object MNotificationManager {
    fun show(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, NotifictionReceiver::class.java)
        intent.action = "musicone_pause_play"
        val play_pause = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val intent2 = Intent(context, NotifictionReceiver::class.java)
        intent2.action = "musicone_next"
        val next = PendingIntent.getBroadcast(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT)


        val intente = Intent(context, NotifictionReceiver::class.java)
        intente.action = "musicone_close"
        val close = PendingIntent.getBroadcast(context, 0, intente, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("MusicOne",
                    "MusicChannel", NotificationManager.IMPORTANCE_LOW)
            channel.enableLights(false)
            channel.enableVibration(false)
            notificationManager.createNotificationChannel(channel)
            NotificationCompat.Builder(context, "MusicOne")
        } else {
            NotificationCompat.Builder(context)
        }
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
        val rv = RemoteViews(context.packageName, R.layout.notifiction_view)
        builder.setCustomContentView(rv)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val style = android.support.v4.media.app.NotificationCompat.MediaStyle()
            builder.setStyle(style)
        }
        builder.priority = NotificationCompat.PRIORITY_MAX
        rv.setImageViewResource(R.id.notifiction_icon, R.mipmap.back2)
        val notification = builder.build()
        val it = Intent(context,PlayerActivity::class.java)
        notification.contentIntent = PendingIntent.getActivity(context,0,it,0)
        rv.setOnClickPendingIntent(R.id.notifiction_toPlay, play_pause)
        rv.setOnClickPendingIntent(R.id.notifiction_next, next)
        rv.setOnClickPendingIntent(R.id.notifiction_close, close)
        notification.flags = Notification.FLAG_NO_CLEAR
        notificationManager.notify(R.string.app_name, notification)
        bandListener(context, rv, notificationManager, notification)
    }

    private fun bandListener(context: Context, rv: RemoteViews, notificationManager: NotificationManager, notification: Notification) {
        Controller.addStateChangeListener { type ->
            when (type) {
                Controller.Type.PAUSE -> {
                    rv.setImageViewResource(R.id.notifiction_toPlay, R.mipmap.toplay)
                }
                Controller.Type.PLAY -> {
                    rv.setImageViewResource(R.id.notifiction_toPlay, R.mipmap.topause)
                }
                Controller.Type.NEXT -> {
                    rv.setImageViewResource(R.id.notifiction_toPlay, R.mipmap.topause)
                }
                Controller.Type.LAST -> {
                    rv.setImageViewResource(R.id.notifiction_toPlay, R.mipmap.topause)
                }
            }
            rv.setTextViewText(R.id.notifiction_title, MusicService.instance?.item?.title)
            rv.setTextViewText(R.id.notifiction_subtitle, MusicService.instance?.item?.getSingers())
            Thread(Runnable {
                val item = MusicService.instance?.item
                val byteArray = if (item?.albumicon?.startsWith("h") == true)
                    NetWork().getBytes(item.albumicon)
                else {
                    try {
                        println(item?.albumicon)
                        File(item?.albumicon).readBytes()
                    } catch (e: Throwable) {
                        NetWork().getBytes("http://musicone-1253269015.coscd.myqcloud.com/9AB05C66154C4C2D975C53B3BFC43E76.png")
                    }
                }
                if (byteArray.size < 1024) return@Runnable
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                RunOnUiThread {
                    try {
                        rv.setImageViewBitmap(R.id.notifiction_icon, bitmap)
                        notificationManager.notify(R.string.app_name, notification)
                    } catch (e: Throwable) {
                    }
                }
            }).start()
        }
    }

    fun dismiss(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(R.string.app_name)
    }
}