package net.coroke.terminal.repository

import net.coroke.terminal.domain.model.Feed
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface FeedRepository : JpaRepository<Feed, Long> {
    @EntityGraph(attributePaths = ["createdUser"])
    fun findAllByBoardIdOrderByIdDesc(pageable: Pageable?, boardId: String?): Page<Feed?>?

    @EntityGraph(attributePaths = ["createdUser"])
    fun findByBoardIdAndAliasId(boardId: String?, aliasId: Int?): Feed?

    @Query("SELECT MAX(aliasId) FROM Feed WHERE boardId = :boardId")
    fun findMaxAliasIdByBoardId(boardId: String): Int?

    @Query("SELECT MAX(aliasId) FROM Feed WHERE boardId = :boardId AND aliasId < :aliasId")
    fun findBackwardAliasIdByAliasIdAndBoardId(boardId: String, aliasId: Int): Int?

    @Query("SELECT MIN(aliasId) FROM Feed WHERE boardId = :boardId AND aliasId > :aliasId")
    fun findForwardAliasIdByAliasIdAndBoardId(boardId: String, aliasId: Int): Int?

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Feed SET hit = hit + 1 WHERE boardId = :boardId AND aliasId = :aliasId")
    fun hitFeedByBoardIdAndAliasId(boardId: String, aliasId: Int)
}
