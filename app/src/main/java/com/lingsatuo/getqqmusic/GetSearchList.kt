package com.lingsatuo.getqqmusic

import android.util.Log
import com.lingsatuo.utils.NetWork
import org.json.JSONObject
import java.util.regex.Pattern

class GetSearchList(private var keywords:String,private var page:Int,private var lis:(Throwable?,ArrayList<MusicItem>)->Unit):Thread() {
    val list = ArrayList<MusicItem>()
    override fun run() {
        val mkeywords = keywords.replace(" ","%20")
        val href = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?ct=24&qqmusic_ver=1298&new_json=1&remoteplace=txt.yqq.top&searchid=0&t=0&aggr=1&cr=1&catZhida=1&lossless=0&flag_qc=0&p=$page&n=20&w=$mkeywords&g_tk=5381&jsonpCallback=MusicJsonCallback09304789982202721&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0"
        try {
            Thread.sleep(300)
            val str = String(NetWork().getBytes(href))
            getlist(str)
            RunOnUiThread{
                lis.invoke(null,list)
            }
        }catch (e:Throwable){
            RunOnUiThread{
                lis.invoke(e,list)
            }
        }
        super.run()
    }
    private fun getlist(jsonSource:String){
        val patterns  = Pattern.compile("\\{[.*\\S\\s]+\\}")
        val matcher = patterns.matcher(jsonSource)
        if(matcher.find()){
            val root = JSONObject(matcher.group())
            val data = root.getJSONObject("data")
            val song = data.getJSONObject("song")
            val listi = song.getJSONArray("list")
            for (index in 0 until listi.length()){
                val obj = listi.getJSONObject(index)
                val item = MusicItem()
                item.singmid = obj.getString("mid")
                item.title = obj.getString("name")
                item.mid = obj.getString("id")
                item.singer = obj.getJSONArray("singer").getJSONObject(0).getString("name")
                item.albumid = obj.getJSONObject("album").getString("mid")
                item.album = obj.getJSONObject("album").getString("name")
                item.href = "https://y.qq.com/n/yqq/song/${item.singmid}.html"
                list.add(item)
            }
        }
    }
}