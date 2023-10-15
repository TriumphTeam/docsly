package dev.triumphteam.docsly.database.exposed

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

public abstract class StringEntity(id: EntityID<String>) : Entity<String>(id)

public abstract class StringEntityClass<out E : StringEntity>(
    table: IdTable<String>,
    entityType: Class<E>? = null,
    entityCtor: ((EntityID<String>) -> E)? = null
) : EntityClass<String, E>(table, entityType, entityCtor)
