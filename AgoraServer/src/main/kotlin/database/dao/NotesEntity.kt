package database.dao

import database.tables.NotesTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class NotesEntity (id: EntityID<Int>) : IntEntity(id){
    companion object : IntEntityClass<NotesEntity>(NotesTable)
    var title by NotesTable.title
    var type by NotesTable.type
    var length by NotesTable.length
    var userId by NotesTable.userId
    var content by NotesTable.content
}