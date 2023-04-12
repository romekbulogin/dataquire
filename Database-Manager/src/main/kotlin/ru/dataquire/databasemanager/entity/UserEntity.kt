package ru.dataquire.databasemanager.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "_user")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(unique = true)
    var username: String? = null

    @Column(unique = true)
    var email: String? = null
    var password: String? = null
    var isActivated: Boolean? = null
    var activatedUUID: String? = null

    @Enumerated(EnumType.STRING)
    var role: Role? = null

    @OneToMany
    var databases: MutableList<DatabaseEntity>? = null
    fun addDatabase(databaseEntity: DatabaseEntity) {
        this.databases?.add(databaseEntity)
    }

    fun deleteDatabase(databaseEntity: DatabaseEntity) {
        this.databases?.remove(databaseEntity)
    }
}
