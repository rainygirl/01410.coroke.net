package net.coroke.terminal.domain.model

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import net.coroke.terminal.domain.dto.CreateUserDto
import net.coroke.terminal.domain.dto.ReadUserDto

@Entity
data class User(
    @Id
    @GeneratedValue
    var id: Long = 0,
    var origin: String,
    var uid: String,
    var username: String,
    var nickname: String?,
    var name: String,
    var email: String,
    var picture: String,
    @Enumerated(EnumType.STRING)
    var role: UserRole

) {

    fun getReadUserDto(): ReadUserDto {
        return ReadUserDto(
            username = username,
            nickname = nickname,
            name = name,
            picture = picture,
            role = role
        )
    }

    fun getCreateUserDto(): CreateUserDto {
        return CreateUserDto(
            origin = origin,
            uid = uid,
            username = username,
            nickname = nickname ?: "",
            name = name,
            email = email,
            picture = picture,
            role = role
        )
    }
}
