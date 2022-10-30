package net.coroke.terminal.domain.dto

import net.coroke.terminal.domain.model.UserRole
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class ReadUserDto(
    var username: String,
    var name: String,
    var nickname: String?,
    var picture: String,
    @Enumerated(EnumType.STRING)
    var role: UserRole
) {}
