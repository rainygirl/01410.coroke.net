package net.coroke.terminal.controller

import com.google.gson.Gson
import net.coroke.terminal.domain.dto.ReadUserDto
import net.coroke.terminal.repository.UserRepository
import net.coroke.terminal.service.AuthService
import net.coroke.terminal.service.Utils
import org.springframework.transaction.annotation.Transactional

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userRepository: UserRepository,
    private val authService: AuthService,
) {

    @PostMapping(value = ["/api/user/nickname"])
    @Transactional
    fun read(
        request: HttpServletRequest,
        response: HttpServletResponse,
        session: HttpSession,
        @RequestBody body: Map<String, String>
    ): String {
        val currentUser = authService.getCurrentUser(request)

        currentUser ?: return "Fail"

        var nickname = body["nickname"] ?: ""
        if (!Pattern.matches("^[ㄱ-ㅎ가-힣a-zA-Z0-9]+$", nickname)) return "Fail"
        if (Utils.length2Bytes(nickname) < 4) return "Fail"

        nickname = Utils.cut2Bytes(nickname, 8)
        if (userRepository.existsByNickname(nickname)) return "Fail"

        userRepository.updateNickname(currentUser.id, nickname)
        val user = userRepository.findByUsername(currentUser.username)?.getReadUserDto()
        val gj = Gson().toJson(user)
        session.setAttribute("user", gj)

        return "ok"
    }

    @GetMapping("/get_user")
    fun getUser(session: HttpSession): ReadUserDto? {
        return authService.getCurrentUserFromSession(session)
    }
}
