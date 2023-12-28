package ru.dataquire.databasemanager.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.sql.Date
import java.util.UUID

@Entity
@Table(name = "owner")
class OwnerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(unique = true, nullable = false)
    var username: String? = null

    @Column(unique = true, nullable = false)
    var email: String? = null

    @Column(nullable = false)
    var password: String? = null

    @Column(nullable = false)
    var isActivated: Boolean? = null

    @Column(nullable = false)
    private var activatedUUID: String? = null

    @CreationTimestamp
    private var registrationDate: Date? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role? = null

    @OneToMany(mappedBy = "ownerEntity")
    var databases: MutableList<DatabaseEntity>? = null

    fun addDatabase(databaseEntity: DatabaseEntity) {
        this.databases?.add(databaseEntity)
    }

    fun deleteDatabase(databaseEntity: DatabaseEntity) {
        this.databases?.remove(databaseEntity)
    }
}
