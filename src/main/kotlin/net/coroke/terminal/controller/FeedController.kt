package net.coroke.terminal.controller

import net.coroke.terminal.domain.dto.CreateFeedDto
import net.coroke.terminal.domain.model.User
import net.coroke.terminal.domain.model.UserRole
import net.coroke.terminal.repository.FeedRepository
import net.coroke.terminal.service.AuthService
import net.coroke.terminal.service.FeedService
import net.coroke.terminal.service.Utils
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import kotlin.math.ceil

@RestController
class FeedController(
    private val feedRepository: FeedRepository,
    private val feedService: FeedService,
    private val authService: AuthService,
) {


    @GetMapping(value = ["/api/board/{boardId}/{aliasId}/hit"])
    @Transactional
    fun hit(
        request: HttpServletRequest,
        response: HttpServletResponse,
        session: HttpSession,
        @PathVariable("boardId") boardId: String,
        @PathVariable("aliasId") aliasId: Int
    ): String {
        val sessionKey = "hit." + (boardId ?: "") + "." + (aliasId ?: "")
        if (session.getAttribute(sessionKey) == 1) return "ok"

        feedRepository.hitFeedByBoardIdAndAliasId(boardId, aliasId)
        session.setAttribute(sessionKey, 1)

        return "ok"
    }

    @GetMapping(value = ["/api/board/{boardId}/{aliasId}/{page}"])
    fun read(
        request: HttpServletRequest,
        response: HttpServletResponse,
        session: HttpSession,
        @PathVariable("boardId") boardId: String,
        @PathVariable("aliasId") aliasId: Int,
        @PathVariable("page") page: Int = 1
    ): String {

        val feed = feedRepository.findByBoardIdAndAliasId(boardId, aliasId)

        feed ?: return ""

        val dateString = feed.createdAt.format(DateTimeFormatter.ofPattern("yy-MM-dd HH:mm"))

        var result = ""
        var text = feed.text

        val paging = 10
        val startLine = (page - 1) * paging
        var endLine = page * paging - 1
        val lineCount = feed.text.split("\n").size

        if (lineCount <= endLine) {
            endLine = lineCount - 1
            text += "|EOF|\n"
        }

        val totalPage = ceil((lineCount / paging.toDouble())).toInt()

        text = text.split("\n").slice(startLine..endLine).joinToString(separator = "\n").replace("<", "&lt;")

        result += "제  목:" + feed.title + "\n올린이:" + feed.name + "   " + dateString + "   분량:" + totalPage + "페이지   읽음:" + feed.hit + "\n" + "-".repeat(
            79
        ) + "\n" + text + "\n"

        return result
    }

    @GetMapping(value = ["/api/board/{boardId}/{page}"])
    fun list(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @PathVariable("boardId") boardId: String,
        @PathVariable("page") page: Int,
        pageable: Pageable
    ): String {
        val size = 12
        val feeds = feedRepository.findAllByBoardIdOrderByIdDesc(PageRequest.of(page - 1, size), boardId)
        var result = " 번 호 이용자명  날 짜 읽음      제      목\n" + "─".repeat(40) + "\n";
        feeds?.forEach { feed ->
            feed ?: return ""

            val id = feed.aliasId.toString().padStart(5, ' ')
            val hit = feed.hit.toString().padStart(4, ' ')
            val date = feed.createdAt.format(DateTimeFormatter.ofPattern("MM-dd"))

            var name = Utils.cut2Bytes(feed.name, 8)
            val nameLength = Utils.length2Bytes(name)
            if (nameLength < 8) name += " ".repeat(8 - nameLength)
            val title = Utils.cut2Bytes(feed.title, 50)

            result += " $id $name  $date $hit  $title\n"
        }
        result += "|[PAGE:" + feeds?.pageable?.pageNumber?.plus(1) + "/" + feeds?.totalPages + "]"

        if (feeds?.pageable?.pageNumber?.plus(1) == feeds?.totalPages) result += "|EOF|"

        return result
    }

    @PostMapping(value = ["/api/board/{boardId}/new"])
    fun create(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestHeader(value = "User-Agent") userAgent: String,
        @PathVariable("boardId") boardId: String,
        @RequestBody feedDto: CreateFeedDto
    ): String {

        val routers = Utils.getRouters()
        if (!routers.keys.contains(boardId) ||
            routers.get(boardId)?.get("type") != "board"
        )
            throw ResourceNotFoundException();

        val createdUser: User? = authService.getCurrentUser(request)

        createdUser ?: throw ResourceNotFoundException();

        feedDto.createdUser = createdUser

        if (routers.get(boardId)?.get("write_permissions") == "admin"
            && createdUser?.role != UserRole.ROLE_ADMIN
        )
            throw ResourceNotFoundException();

        feedDto.boardId = boardId
        feedDto.createdIp = request.remoteAddr
        feedDto.createdAgent = userAgent
        feedDto.name = createdUser?.nickname ?: ""

        feedService.createFeed(feedDto)

        return "ok"
    }
}
