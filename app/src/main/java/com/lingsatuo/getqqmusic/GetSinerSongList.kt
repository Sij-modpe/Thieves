package com.lingsatuo.getqqmusic

import com.lingsatuo.getqqmusic.mv.MvItem
import com.lingsatuo.utils.NetWork
import org.json.JSONArray
import org.json.JSONObject
import java.util.regex.Pattern


class GetSinerSongList(private var singer: MusicItem.Singer, private val page: Int, private var lis: (Throwable?, ArrayList<MusicItem>) -> Unit) : Thread() {
    override fun run() {
        try {
           val list =  Get().get()
            RunOnUiThread{
                lis.invoke(null,list)
            }
        } catch (e: Throwable) {
            RunOnUiThread{
                lis.invoke(e,ArrayList())
            }
        }
        super.run()
    }

    inner class Get {
        private val listi = ArrayList<MusicItem>()
        fun get(): ArrayList<MusicItem> {
            listi.clear()
            val href = "https://c.y.qq.com/v8/fcg-bin/fcg_v8_singer_track_cp.fcg?g_tk=5381&" +
                    "jsonpCallback=MusicJsonCallbacksinger_track&" +
                    "loginUin=0&hostUin=0&format=jsonp&" +
                    "inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq" +
                    "&needNewCode=0&singermid=" +
                    "${singer.singermid}&" +
                    "order=listen&begin=$page&num=30&songstatus=1"
            val jsp = String(NetWork().getBytes(href))
            sp(jsp)
            return listi
        }

        private fun sp(str: String) {
            val patterns = Pattern.compile("\\{[.*\\S\\s]+\\}")
            val matcher = patterns.matcher(str)
            if (matcher.find()) {
                val root = JSONObject(matcher.group())
                val data = root.getJSONObject("data")
                val list = data.getJSONArray("list")
                for (index in 0 until list.length()) {
                    val musicitem = MusicItem()
                    val musicdata = list.getJSONObject(index).getJSONObject("musicData")
                    musicitem.album = musicdata.getString("albumname")
                    musicitem.albummid = musicdata.getString("albummid")
                    musicitem.icon = musicitem.albummid
                    musicitem.title = musicdata.getString("songname")
                    musicitem.singmid = musicdata.getString("songmid")
                    musicitem.strMediaMid = musicdata.getString("strMediaMid")
                    if (musicdata.getString("vid")!=""){
                        musicitem.mvItem = MvItem()
                        musicitem.mvItem!!.mvid = musicdata.getString("vid")
                    }
                    getFileSize(musicitem ,musicdata)
                    musicitem.href = "https://y.qq.com/n/yqq/song/${musicitem.singmid}.html"
                    getSinger(musicitem, musicdata.getJSONArray("singer"))
                    listi.add(musicitem)
                }
            }
        }



        private fun getFileSize(item: MusicItem,file:JSONObject){
         val mp3h = file.getInt("size320")
            if (mp3h!=0)
                item.filesize.put(GetMusicFileName.Quality.MP3H,"${String.format("%.2f",(mp3h/1024f/1024))}MB")
            val mp3 = file.getInt("size128")
            if (mp3!=0)
                item.filesize.put(GetMusicFileName.Quality.MP3,"${String.format("%.2f",(mp3/1024f/1024))}MB")
            val ape = file.getInt("sizeape")
            if (ape!=0)
                item.filesize.put(GetMusicFileName.Quality.APE,"${String.format("%.2f",(ape/1024f/1024))}MB")
            val flac = file.getInt("sizeflac")
            if (flac!=0)
                item.filesize.put(GetMusicFileName.Quality.FLAC,"${String.format("%.2f",(flac/1024f/1024))}MB")
            val ogg = file.getInt("sizeogg")
            if (ogg!=0)
                item.filesize.put(GetMusicFileName.Quality.OGG,"${String.format("%.2f",(ogg/1024f/1024))}MB")
        }
        private fun getSinger(musicItem: MusicItem, singers: JSONArray) {
            for (a in 0 until singers.length()) {
                val sings = MusicItem.Singer()
                sings.singer = singers.getJSONObject(a).getString("name")
                sings.singerid = singers.getJSONObject(a).getInt("id")
                sings.singermid = singers.getJSONObject(a).getString("mid")
                sings.singerhref = "https://y.qq.com/n/yqq/singer/${sings.singermid}.html"
                musicItem.singer.add(sings)
            }
        }
    }
}