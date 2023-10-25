package ru.dataquire.instancekeeper.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "instance")
data class InstanceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false)
    var url: String? = null,

    @Column(nullable = false)
    var username: String? = null,

    @Column(nullable = false)
    var password: String? = null,

    @Column(nullable = false)
    var dbms: String? = null,
)
