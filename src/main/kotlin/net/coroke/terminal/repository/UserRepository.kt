package net.coroke.terminal.repository

import net.coroke.terminal.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query


interface UserRepository : JpaRepository<User, Long> {

    fun findByUsername(username: String): User?
    fun findByOriginAndUid(origin: String, uid: String): User?

    fun existsByUsername(username: String): Boolean
    fun existsByNickname(nickname: String): Boolean

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User SET nickname = :nickname WHERE id= :id")
    fun updateNickname(id: Long, nickname: String)

}
