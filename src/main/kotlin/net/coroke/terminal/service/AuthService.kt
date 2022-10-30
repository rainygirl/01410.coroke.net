package net.coroke.terminal.service

import com.google.gson.Gson
import net.coroke.terminal.domain.dto.CreateUserDto
import net.coroke.terminal.domain.dto.ReadUserDto
import net.coroke.terminal.domain.model.User
import net.coroke.terminal.domain.model.UserRole
import net.coroke.terminal.repository.UserRepository
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import javax.transaction.Transactional

@Service
class AuthService(
    private val userService: UserService,
    private val userRepository: UserRepository
) {

    fun isAuthenticated(session: HttpSession?): Boolean {
        return (session?.getAttribute("user") != null)
    }

    fun getCurrentUserFromSession(session: HttpSession?): ReadUserDto? {
        if (!isAuthenticated(session)) return null

        val gj = session?.getAttribute("user")
        gj ?: return null

        val user: ReadUserDto = Gson().fromJson(gj.toString(), ReadUserDto::class.java)
        return user
    }

    fun getCurrentUser(request: HttpServletRequest): User? {
        val session: HttpSession? = request.getSession(false) ?: null
        session ?: return null

        val userDto: ReadUserDto? = getCurrentUserFromSession(session)
        userDto ?: return null

        return userRepository.findByUsername(userDto.username)
    }

    @Transactional
    fun oAuth1Login(oAuth1User: Map<String, String>): ReadUserDto {

        val userDto = CreateUserDto(
            username = oAuth1User["username"] as String,
            role = UserRole.ROLE_USER,
            origin = oAuth1User["origin"] as String,
            uid = oAuth1User["uid"] as String,
            email = oAuth1User["email"] as String,
            name = oAuth1User["name"] as String,
            picture = oAuth1User["picture"] as String,
            nickname = ""
        )

        val user = userService.getOrCreateUser(userDto)

        return user.toEntity().getReadUserDto()
    }

    @Transactional
    fun oAuth2Login(oAuth2User: OAuth2User): ReadUserDto {
        var origin = ""
        var sub = ""

        if (oAuth2User.attributes["iss"].toString() == "https://accounts.google.com") {
            sub = oAuth2User.attributes["sub"] as String
            origin = "google"
        }

        val usernameCandidate = (oAuth2User.attributes["email"] as String).split('@')[0]

        val userDto = CreateUserDto(
            username = usernameCandidate,
            role = UserRole.ROLE_USER,
            origin = origin,
            uid = sub,
            email = oAuth2User.attributes["email"] as String,
            name = oAuth2User.attributes["name"] as String,
            picture = oAuth2User.attributes["picture"] as String,
            nickname = ""
        )

        val user = userService.getOrCreateUser(userDto)
        return user.toEntity().getReadUserDto()
    }
}
