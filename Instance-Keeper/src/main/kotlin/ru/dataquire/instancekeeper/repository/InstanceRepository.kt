package ru.dataquire.instancekeeper.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.dataquire.instancekeeper.entity.InstanceEntity

@Repository
interface InstanceRepository : JpaRepository<InstanceEntity, Int> {
    fun findByDbms(dbms: String): InstanceEntity
}