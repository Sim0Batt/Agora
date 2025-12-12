package database

import database.tables.NotesTable
import database.tables.UsersTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import server.models.AccountJson
import server.models.CreateNoteJson
import server.models.NotesJson
import server.models.SaveNoteJson
import server.models.UserJson

object QueryManager {
    val connection = DatabaseConfig.getConnection()
    fun getUsers(): List<UserJson>{
        val tmp = mutableListOf<UserJson>()
        transaction (connection){
            UsersTable.selectAll().map { user ->
                tmp.add(UserJson(user[UsersTable.name], user[UsersTable.password]))
            }
        }
        return tmp
    }

    fun getSpecificUser(id: Int): UserJson{
        var userJson = UserJson("", "")
        transaction (connection){
            userJson = UserJson(
                UsersTable.selectAll().where { UsersTable.id eq id }.firstOrNull()?.get(UsersTable.name).toString(),
                UsersTable.selectAll().where { UsersTable.id eq id }.firstOrNull()?.get(UsersTable.password).toString()
            )
        }
        return userJson
    }

    fun createUser(name: String, password: String){
        try {
            transaction (connection){
                UsersTable.insert {
                    it[UsersTable.name] = name
                    it[UsersTable.password] = password
                }
            }
        }catch (e: Exception){
            println(e.message)
        }
    }

    fun loginUser(userJson: UserJson): AccountJson{
        var account = AccountJson("null", -1)
        transaction (connection){
            val user = UsersTable.selectAll().where { UsersTable.name eq userJson.name }.firstOrNull()?.get(UsersTable.name)
            val id = UsersTable.selectAll().where { UsersTable.name eq userJson.name }.firstOrNull()?.get(UsersTable.id)
            account = AccountJson(user.toString(), id.toString().toInt())
        }
        return account
    }


    fun getNotes(): List<NotesJson>{
        val tmp = mutableListOf<NotesJson>()
        transaction (connection){
            NotesTable.selectAll().map { note ->
                tmp.add(
                    NotesJson(
                        note[NotesTable.id].toString().toInt(),
                        note[NotesTable.title],
                        note[NotesTable.type],
                        note[NotesTable.length],
                        note[NotesTable.userId]
                    )
                )
            }
        }
        return tmp
    }

    fun getUserNotes(userId: Int): List<NotesJson>{
        val tmp = mutableListOf<NotesJson>()
        transaction (connection){
            NotesTable.selectAll().where{
              NotesTable.userId eq userId
            }.map { note ->
                tmp.add(
                    NotesJson(
                        note[NotesTable.id].toString().toInt(),
                        note[NotesTable.title],
                        note[NotesTable.type],
                        note[NotesTable.length],
                        note[NotesTable.userId],
                        note[NotesTable.content]
                            .replace("\n", "\\n")
                            .replace("\"",  "'")
                            .replace("\\", "\\\\")
                    )
                )
            }
        }
        return tmp
    }

    fun deleteNote(id: Int){
        transaction (connection){
            NotesTable.deleteWhere { NotesTable.id eq id }
        }
    }

    fun createNote(notesJson: CreateNoteJson){
        transaction (connection){
            NotesTable.insert {
                it[NotesTable.title] = notesJson.title
                it[NotesTable.type] = notesJson.type
                it[NotesTable.length] = notesJson.length
                it[NotesTable.userId] = notesJson.userId
                it[NotesTable.content] = ""
            }
        }
    }

    fun updateNote(notesJson: SaveNoteJson){
        transaction (connection){
            NotesTable.update({ NotesTable.id eq notesJson.noteId}){
                it[NotesTable.content] = notesJson.content
                it[NotesTable.length] = notesJson.length
            }
        }
    }

    fun getContent(noteId: Int): String{
        var content = ""
        transaction (connection){
            content = NotesTable.selectAll().where{ NotesTable.id eq noteId }.firstOrNull()!![NotesTable.content]
                .replace("\n", "\\n")
                .replace("\"",  "'")
                .replace("\\", "\\\\")
        }
        return content
    }
}