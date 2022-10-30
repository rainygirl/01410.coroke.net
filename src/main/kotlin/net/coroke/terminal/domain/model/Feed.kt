package net.coroke.terminal.domain.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set
}


@Entity
@EntityListeners(AuditingEntityListener::class)
data class Feed(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,
    var boardId: String,
    var aliasId: Int,

    @OneToOne
    var createdUser: User?,
    var name: String,
    var title: String,
    var text: String,

    var createdIp: String,
    var createdAgent: String,

    var hit: Int
) : BaseTimeEntity() {
}

