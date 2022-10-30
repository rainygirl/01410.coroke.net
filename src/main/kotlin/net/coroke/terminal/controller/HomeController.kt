package net.coroke.terminal.controller

import net.coroke.terminal.service.Utils
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class HomeController {

    @RequestMapping(value = ["/{pwd:[a-z]+}/{id:[0-9]+}", "/{pwd:[a-z]+}"])
    fun pwd(@PathVariable pwd: String, @PathVariable id: Int?): String {
        val routers = Utils.getRouters()
        if (!routers.keys.contains(pwd)) throw ResourceNotFoundException()
        return "/index.html"
    }

}
