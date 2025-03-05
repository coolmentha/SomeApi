package org.example.mirai.plugin

enum class Command (val command:String,val method: String){
    CountdownToHoliday("#节日倒计时","dealWithCountdownToHoliday"),
    SIXTY_S_WORLD("#看世界","dealWith60SWorld"),
    WEATHER("#天气","dealWithWeather");

    companion object {
        fun getCommand(command:String):Command?{
            return values().find { it.command == command }
        }
    }
}