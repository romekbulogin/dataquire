package ru.dataquire.authorizationservice.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.sql.Timestamp
import java.util.*

@Entity
@Table(name = "query_history")
class QueryHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    lateinit var id: UUID

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    lateinit var executionDate: Timestamp

    @Column(nullable = false)
    lateinit var query: String

    @ManyToOne
    lateinit var ownerEntity: OwnerEntity
}