package net.coroke.terminal.service

import net.coroke.terminal.domain.dto.CreateUserDto
import net.coroke.terminal.domain.model.User
import net.coroke.terminal.repository.UserRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {

    @Transactional
    fun getOrCreateUser(user: CreateUserDto): CreateUserDto {

        val currentUser: User? = userRepository.findByOriginAndUid(user.origin, user.uid)

        if (currentUser != null)
            return currentUser.getCreateUserDto()

        user.username = getUniqueUsername(user.username)

        userRepository.save(user.toEntity())

        return user
    }

    @Transactional
    fun getUniqueUsername(originName: String): String {
        var username: String = originName
        var index = 0
        while (userRepository.existsByUsername(username)) {
            index++
            username = "$originName$index"
        }
        return username
    }
}
