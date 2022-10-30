package net.coroke.terminal.handler

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class FailureHandler : AuthenticationFailureHandler {

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {

        var errorId = ""
        if (exception is OAuth2AuthenticationException) {
            errorId = "ERR-0002"
        } else if (exception is BadCredentialsException) {
            errorId = "ERR-0001"
        }

        response.sendRedirect(request.contextPath + "/#" + errorId)
    }
}
