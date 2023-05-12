package ru.dataquire.databasemanager.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "_databases")
data class DatabaseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var dbms: String? = null,
    @Column(unique = true)
    var systemName: String? = null,
    var databaseName: String? = null,
    var login: String? = null,
    var passwordDbms: String? = null,
    var url: String? = null,
    @ManyToOne
    var userEntity: UserEntity? = null
)
