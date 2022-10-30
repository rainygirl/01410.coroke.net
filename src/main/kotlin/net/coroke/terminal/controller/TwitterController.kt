package net.coroke.terminal.controller

import com.google.gson.Gson
import net.coroke.terminal.domain.dto.ReadUserDto
import net.coroke.terminal.service.AuthService
import net.coroke.terminal.service.TwitterService
import org.springframework.social.connect.Connection
import org.springframework.social.twitter.api.Twitter
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.view.RedirectView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

@Controller
class TwitterController(
    private val authService: AuthService,
    private val twitterService: TwitterService
) {
    @GetMapping(value = ["\${twitter.authorization-uri}"])
    fun twitterAuthorize(request: HttpServletRequest, response: HttpServletResponse) {
        response.sendRedirect(twitterService.getAuthenticationUrl(request))
    }

    @GetMapping(value = ["\${twitter.callback-uri}"])
    fun twitterComplete(
        request: HttpServletRequest,
        session: HttpSession,
        @RequestParam(name = "oauth_token") oauthToken: String,
        @RequestParam(name = "oauth_verifier") oauthVerifier: String
    ): RedirectView {

        val connection: Connection<Twitter> = twitterService.getAccessTokenToConnection(request, oauthVerifier)

        val user: ReadUserDto = authService.oAuth1Login(twitterService.getUserProfileMap(connection))
        val gj = Gson().toJson(user);

        session.setAttribute("user", gj)

        return RedirectView("/")
    }
}
