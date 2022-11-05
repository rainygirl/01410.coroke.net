package net.coroke.terminal.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.coroke.terminal.controller.HomeController

object Utils {

    fun cut2Bytes(s: String, maxLength: Int): String {
        var l = 0
        var t = ""
        s.toCharArray().forEach {
            l += if (it.code < 255) 1 else 2
            if (l <= maxLength) t += it
        }
        return t
    }

    fun length2Bytes(s: String): Int {
        val c: CharArray = s.toCharArray()
        val m: List<Int> = c.map { (if (it.code < 255) 1 else 2) }
        return m.sum()
    }

    fun getRouters(): Map<String, Map<String, Any>> {
        val jsonString = HomeController::class.java.getResource("/static/routes.json")?.readText()
        return Gson().fromJson(jsonString, object : TypeToken<Map<String, Map<String, Any>>>() {}.type)
    }
}
