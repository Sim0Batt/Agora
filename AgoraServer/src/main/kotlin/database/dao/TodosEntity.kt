package database.dao

import database.tables.TodosTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class TodosEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<TodosEntity>(TodosTable)

    var title by TodosTable.title
    var description by TodosTable.description
    var expirationDate by TodosTable.expirationDate
    var type by TodosTable.type

}