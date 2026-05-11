package database.dao

import database.tables.LatexNotesTable
import database.tables.NotesTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class LatexNotesEntity (id: EntityID<Int>) : IntEntity(id){
    companion object : IntEntityClass<LatexNotesEntity>(LatexNotesTable)
    var title by LatexNotesTable.title
    var userId by LatexNotesTable.userId
    var content by LatexNotesTable.content
    var type by LatexNotesTable.type
}