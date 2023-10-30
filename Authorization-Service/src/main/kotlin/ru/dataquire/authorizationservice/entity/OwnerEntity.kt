package ru.dataquire.authorizationservice.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.sql.Date
import java.util.UUID

@Entity
@Table(name = "owner")
class OwnerEntity : UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private var id: UUID? = null

    @Column(unique = true, nullable = false)
    private var username: String? = null

    @Column(unique = true, nullable = false)
    private var email: String? = null

    @Column(nullable = false)
    private var password: String? = null

    @Column(nullable = false)
    private var isActivated: Boolean? = null

    @Column(nullable = false)
    private var activatedUUID: String? = null

    @CreationTimestamp
    private var registrationDate: Date? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private var role: Role? = null

    @OneToMany(mappedBy = "ownerEntity")
    private var databases: MutableList<DatabaseEntity>? = null

    fun setActivatedUUID(uuid: String) {
        this.activatedUUID = uuid
    }

    fun getActivatedUUID() = this.activatedUUID

    fun addDatabase(databaseEntity: DatabaseEntity) {
        this.databases?.add(databaseEntity)
    }

    fun deleteDatabase(databaseEntity: DatabaseEntity) {
        this.databases?.remove(databaseEntity)
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun getEmail() = this.email
    fun getNickname() = this.username
    fun getIsActivated() = this.isActivated
    fun getRole(): Role? = this.role
    fun setPassword(password: String) {
        this.password = password
    }

    fun setIsActivated(isActivated: Boolean) {
        this.isActivated = isActivated
    }

    fun setRole(role: Role) {
        this.role = role
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        mutableListOf(SimpleGrantedAuthority(role?.name))

    override fun getPassword(): String? = password

    override fun getUsername(): String? = this.email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
