package org.example.mirai.plugin

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Part
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import okhttp3.OkHttpClient
import java.io.File


class CommandService {
    private val client = OkHttpClient()
    private val logger = PluginMain.logger
    private val objectMapper = ObjectMapper()
    private val first = Bot.instances[0].friends.first()
    init {
        objectMapper.registerModule(Jdk8Module()) // 注册 Java 8 时间类型模块
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY) // 忽略空字段
    }



    suspend fun dealWithCountdownToHoliday(content: String): MessageChain {
        val url = "https://api.andeer.top/API/countdown.php"
        val body = HttpUtils.execute(url)
        val parseToJsonElement = body?.let { Json.parseToJsonElement(it) }
        val content = parseToJsonElement?.jsonObject?.get("data")?.jsonPrimitive?.content

        return buildMessageChain { +content!! }
    }

        suspend fun dealWith60SWorld(content: String): MessageChain {
        val url = "https://api.52vmy.cn/api/wl/60s"
        var toExternalResource = HttpUtils.downloadBytes(url)?.toExternalResource()
        var uploadImage = first.uploadImage(toExternalResource!!)
        if (toExternalResource == null){
            logger.error("60s图片下载失败")
            return buildMessageChain { +"60s图片下载失败" }
        }
        return buildMessageChain { +uploadImage }
    }
    suspend fun dealWithWeather(content: String): MessageChain {
        content.split(" ")
        if (content.split(" ").size != 2) {
            return buildMessageChain { +"参数错误，请输入城市名" }
        }
        val city = content.split(" ")[1]
        val url =
            "https://api.seniverse.com/v3/weather/daily.json?key=SpA_Vw80FPBoYBCtd&location=$city&language=zh-Hans&unit=c&start=0&days=4"
        val body = HttpUtils.execute(url) ?: return buildMessageChain { +"天气信息获取失败,请检查参数" }
        val formatJsonToChinese = formatJsonToChinese(body)

        return buildMessageChain { +formatJsonToChinese }
    }

    private fun formatJsonToChinese(jsonStr: String): String {
        val json = Json.parseToJsonElement(jsonStr).jsonObject
        val results = json["results"]?.jsonArray
        val result = results?.get(0)?.jsonObject
        val location = result?.get("location")?.jsonObject
        val daily = result?.get("daily")?.jsonArray


        val locationInfo = buildString {
            append("地点：")
            location?.get("name")?.jsonPrimitive?.content?.let { append(it) }
            append("，所属国家：")
            location?.get("country")?.jsonPrimitive?.content?.let { append(it) }
            append("，时区：")
            location?.get("timezone")?.jsonPrimitive?.content?.let { append(it) }
        }
        var dailyInfos = ""
        if (daily != null) {
            for (dailyWeatherElement in daily) {
                val dailyWeather = dailyWeatherElement.jsonObject
                val dailyInfo = buildString {
                    append("日期：")
                    dailyWeather["date"]?.jsonPrimitive?.content?.let { append(it) }
                    append("\n白天天气：")
                    dailyWeather["text_day"]?.jsonPrimitive?.content?.let { append(it) }
                    append("，\n夜晚天气：")
                    dailyWeather["text_night"]?.jsonPrimitive?.content?.let { append(it) }
                    append("，\n最高气温：")
                    dailyWeather["high"]?.jsonPrimitive?.content?.let { append(it) }
                    append("℃，\n最低气温：")
                    dailyWeather["low"]?.jsonPrimitive?.content?.let { append(it) }
                    append("℃，\n降雨量：")
                    dailyWeather["rainfall"]?.jsonPrimitive?.content?.let { append(it) }
                    append("mm，\n降水量：")
                    dailyWeather["precip"]?.jsonPrimitive?.content?.let { append(it) }
                    append("mm，\n风向：")
                    dailyWeather["wind_direction"]?.jsonPrimitive?.content?.let { append(it) }
                    append("（")
                    dailyWeather["wind_direction_degree"]?.jsonPrimitive?.content?.let { append(it) }
                    append("°），\n风速：")
                    dailyWeather["wind_speed"]?.jsonPrimitive?.content?.let { append(it) }
                    append("，\n风力等级：")
                    dailyWeather["wind_scale"]?.jsonPrimitive?.content?.let { append(it) }
                    append("，\n湿度：")
                    dailyWeather["humidity"]?.jsonPrimitive?.content?.let { append(it) }
                    append("%")
                }
                dailyInfos += dailyInfo + "\n"
            }
        }


        val lastUpdate = buildString {
            append("最后更新时间：")
            result?.get("last_update")?.jsonPrimitive?.content?.let { append(it) }
        }

        return "$locationInfo\n$dailyInfos\n$lastUpdate"
    }


}

