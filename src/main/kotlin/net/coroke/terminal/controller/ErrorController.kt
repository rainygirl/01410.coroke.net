package net.coroke.terminal.controller

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping


@Controller
class MyErrorController : ErrorController {
    @RequestMapping(value = ["/error"])
    fun error(): String {
        return "/index.html"
    }
}