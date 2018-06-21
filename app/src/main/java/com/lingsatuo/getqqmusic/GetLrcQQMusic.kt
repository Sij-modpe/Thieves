package com.lingsatuo.getqqmusic

import com.lingsatuo.utils.NetWork
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class GetLrcQQMusic(private var item:MusicItem,private var lis:(String,Throwable?)->Unit):Thread() {
    private val href = "https://u.y.qq.com/cgi-bin/musicu.fcg?_=1529342495335"
    private val songid = "https://c.y.qq.com/v8/fcg-bin/fcg_play_single_song.fcg?songmid=${item.singmid}&tpl=yqq_song_detail&format=jsonp&callback=getOneSongInfoCallback&g_tk=5381&jsonpCallback=getOneSongInfoCallback&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0"
    override fun run() {
        super.run()
        try {
            val jsonSource = String(NetWork().getBytes(songid))
            val id = getid(jsonSource)
            item.singid = id
            val json = "{\"comm\":{\"g_tk\":5381,\"uin\":0,\"format\":\"json\",\"inCharset\":" +
                    "\"utf-8\",\"outCharset\":\"utf-8\",\"notice\":0," +
                    "\"platform\":\"h5\",\"needNewCode\":1},\"song_detail\"" +
                    ":{\"module\":\"music.pf_song_detail_svr\"," +
                    "\"method\":\"get_song_detail\"," +
                    "\"param\":{\"song_id\":${item.singid}}}}"
            val url = URL(href)
            val huc = url.openConnection() as HttpURLConnection
            huc.doOutput = true
            huc.doInput = true
            huc.useCaches = false
            huc.requestMethod = "GET"
            huc.setRequestProperty("Charset", "UTF-8")
            huc.setRequestProperty("contentType", "application/json")
            huc.connect()
            val os = huc.outputStream
            os.write(json.toByteArray())
            os.flush()
            os.close()
            val iis = huc.inputStream
            val buffered = BufferedReader(InputStreamReader(iis, "UTF-8"))
            var s = StringBuffer()
            while (true) {
                val line = buffered.readLine() ?: break
                s.append(line)
            }
            buffered.close()
            iis.close()
            val lrc = getLrc(s.toString())
            huc.disconnect()
            RunOnUiThread{
                lis.invoke(lrc,null)
            }
        }catch (e:Throwable){
            RunOnUiThread{
                lis.invoke("",e)
            }
        }
    }
    private fun getid(jsonSource:String):Int{
        val patterns  = Pattern.compile("\\{[.*\\S\\s]+\\}")
        val matcher = patterns.matcher(jsonSource)
        if (matcher.find()) {
            val root = matcher.group()
            val json = JSONObject(root)
            val data = json.getJSONArray("data")
            val id = data.getJSONObject(0).getInt("id")
            return id
        }
        return 0
    }
    private fun getLrc(jsonSource: String):String{
        val root = JSONObject(jsonSource)
        val song_detail = root.getJSONObject("song_detail")
        val data = song_detail.getJSONObject("data")
        val info = data.getJSONArray("info")
        for (index in 0 until info.length()){
            if (info.getJSONObject(index).getString("type")=="lyric"){
                val content = info.getJSONObject(index).getJSONArray("content")
                val lrc = content.getJSONObject(0).getString("value").replace("\\n","\n")
                return lrc
            }
        }
        return ""
    }
}