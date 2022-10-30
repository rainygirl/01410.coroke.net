package net.coroke.terminal.controller

import com.google.gson.Gson
import net.coroke.terminal.domain.dto.ReadUserDto
import net.coroke.terminal.service.AuthService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

@RestController
class AuthController(private val authService: AuthService) {

    @GetMapping("/login")
    fun oAuth2Login(): RedirectView {
        return RedirectView("/")
    }

    @GetMapping("/login/oauth2/callback")
    fun oAuth2LoginCallback(@AuthenticationPrincipal oAuth2User: OAuth2User, session: HttpSession): RedirectView {
        val user: ReadUserDto = authService.oAuth2Login(oAuth2User)
        val gj = Gson().toJson(user)
        session.setAttribute("user", gj)

        return RedirectView("/")
    }

    @RequestMapping(path = ["/logout"], method = [RequestMethod.GET, RequestMethod.POST])
    fun logout(response: HttpServletResponse, session: HttpSession): String {
        val cookie = Cookie("SESSION", null);
        cookie.setMaxAge(0)
        cookie.setPath("/")
        response.addCookie(cookie)

        session.setAttribute("user", null)
        return "ok"
    }

}
