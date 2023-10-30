package ru.dataquire.databasemanager.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "databases")
data class DatabaseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false)
    var dbms: String? = null,

    @Column(unique = true, nullable = false)
    var systemName: String? = null,

    @Column(nullable = false)
    var databaseName: String? = null,

    @Column(nullable = false)
    var login: String? = null,

    @Column(nullable = false)
    var passwordDbms: String? = null,

    @Column(nullable = false)
    var url: String? = null,

    @Column(nullable = false)
    var isImported: Boolean? = null,

    @ManyToOne
    var ownerEntity: OwnerEntity? = null
)
