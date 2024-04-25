package ru.dataquire.databasemanager.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "databases")
class DatabaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    lateinit var id: UUID

    @Column(nullable = false)
    lateinit var dbms: String

    @Column(unique = true, nullable = false)
    lateinit var title: String

    @Column(nullable = false)
    lateinit var schema: String

    @Column(nullable = false)
    lateinit var url: String
}
