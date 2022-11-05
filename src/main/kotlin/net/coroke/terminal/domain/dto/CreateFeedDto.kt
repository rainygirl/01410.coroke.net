package net.coroke.terminal.domain.dto

import net.coroke.terminal.domain.model.Feed
import net.coroke.terminal.domain.model.User
import javax.persistence.CascadeType
import javax.persistence.OneToOne

data class CreateFeedDto(
    var boardId: String? = "",
    var aliasId: Int? = 0,
    @OneToOne(cascade = [CascadeType.ALL])
    var createdUser: User?,
    var name: String? = "",
    var title: String,
    var text: String,
    var createdIp: String? = "",
    var createdAgent: String? = "",
) {

    fun toEntity(): Feed {
        return Feed(
            id = 0,
            aliasId = aliasId ?: 0,
            createdIp = createdIp ?: "",
            createdAgent = createdAgent ?: "",
            boardId = boardId ?: "",
            createdUser = createdUser,
            name = name ?: "",
            title = title,
            text = text,
            hit = 0,
        )
    }
}
