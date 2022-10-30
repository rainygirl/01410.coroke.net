package net.coroke.terminal.domain.dto

import net.coroke.terminal.domain.model.User
import net.coroke.terminal.domain.model.UserRole
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class CreateUserDto(
    val id: Long? = null,
    var origin: String,
    var uid: String,
    var username: String,
    var name: String,
    var nickname: String?,
    var email: String,
    var picture: String,
    @Enumerated(EnumType.STRING)
    var role: UserRole
) {

    fun toEntity(): User {
        return User(
            id = id ?: 0,
            origin = origin,
            uid = uid,
            username = username,
            nickname = nickname,
            name = name,
            email = email,
            picture = picture,
            role = role
        )
    }
}
