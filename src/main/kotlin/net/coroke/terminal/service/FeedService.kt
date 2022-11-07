package net.coroke.terminal.service

import net.coroke.terminal.domain.dto.CreateFeedDto
import net.coroke.terminal.repository.FeedRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class FeedService(
    private val feedRepository: FeedRepository
) {

    @Transactional
    fun createFeed(feed: CreateFeedDto): CreateFeedDto {
        val lastAliasId = feedRepository.findMaxAliasIdByBoardId(feed.boardId ?: "")

        feed.aliasId = if (lastAliasId == null) 1 else (lastAliasId + 1)
        feed.text = feed.text.replace("<", "&lt;")
        feed.title = feed.title.replace("<", "&lt;")

        feedRepository.save(feed.toEntity())
        return feed
    }
}
