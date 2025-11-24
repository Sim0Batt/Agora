package database.dao

import database.tables.NotesTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class NotesEntity (id: EntityID<Int>) : IntEntity(id){
    companion object : IntEntityClass<NotesEntity>(NotesTable)
    var title by NotesTable.title
    var type by NotesTable.type
    var length by NotesTable.length
    var path by NotesTable.path
    var userId by NotesTable.userId
}