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
    var id: UUID? = null

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    var executionDate: Timestamp? = null

    @Column(nullable = false)
    var query: String? = null

    @ManyToOne
    var ownerEntity: OwnerEntity? = null
}