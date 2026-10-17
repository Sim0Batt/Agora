package database.dao

import database.tables.LatexNotesTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class LatexNotesEntity (id: EntityID<Int>) : IntEntity(id){
    companion object : IntEntityClass<LatexNotesEntity>(LatexNotesTable)
    var title by LatexNotesTable.title
    var userId by LatexNotesTable.userId
    var content by LatexNotesTable.content
    var type by LatexNotesTable.type
}