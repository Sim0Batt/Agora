package database.dao

import database.tables.UsersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class UserEntity (id: EntityID<Int>) : IntEntity(id){
    companion object : IntEntityClass<UserEntity>(UsersTable)
    var name by UsersTable.name
    var password by UsersTable.password
}