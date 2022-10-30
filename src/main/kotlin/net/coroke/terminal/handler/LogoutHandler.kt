package net.coroke.terminal.handler

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.web.authentication.logout.LogoutHandler as LogoutHandler_

@Service
class LogoutHandler : LogoutHandler_ {

    override fun logout(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication?) {
        request.session.setAttribute("user", null)
    }
}
