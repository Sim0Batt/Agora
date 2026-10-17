package database

import database.tables.LatexNotesTable
import database.tables.NotesTable
import database.tables.TodosTable
import database.tables.UsersTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import server.JSONmodels.AccountJson
import server.JSONmodels.CreateNoteJson
import server.JSONmodels.InfoLatexJson
import server.JSONmodels.LatexNoteJson
import server.JSONmodels.NotesJson
import server.JSONmodels.SaveNoteJson
import server.JSONmodels.TodoJson
import server.JSONmodels.UserJson
import java.time.LocalDate

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
                it[NotesTable.length] = notesJson.length!!
            }
        }
    }

    fun getNotesContent(noteId: Int): String{
        var content = ""
        transaction (connection){
            content = NotesTable.selectAll().where{ NotesTable.id eq noteId }.firstOrNull()!![NotesTable.content]
                .replace("\n", "\\n")
                .replace("\"",  "'")
                .replace("\\", "\\\\")
        }
        return content
    }


    fun createLatexNote(latexNoteJson: LatexNoteJson): Int{
        val initialLatex = """
\documentclass{article}
\usepackage{graphicx} % Required for inserting images
\usepackage{geometry}
\usepackage{subcaption}
\geometry{a4paper, top=1 cm, bottom=1.5cm, left=1.5cm, right=2cm, bindingoffset=5mm}
\newenvironment{sistema}% 
{\left\lbrace\begin{array}{@{}l@{}}}% 
{\end{array}\right.} 
\usepackage{hyperref}
\usepackage{color}
\usepackage{amssymb}
\usepackage{amsmath}
\usepackage{textcomp}
\hypersetup{
    colorlinks=true, 
    linktoc=all,     
    linkcolor=black,  
}
\DeclareMathSizes{11}{11}{11}{11}
\definecolor{dkgreen}{rgb}{0,0.6,0}
\definecolor{gray}{rgb}{0.5,0.5,0.5}
\definecolor{mauve}{rgb}{0.58,0,0.82}
\usepackage{xcolor}
\colorlet{keyword}{blue!100!black!80}
\colorlet{comment}{green!90!black!90}
\usepackage{listings}

\lstdefinelanguage{C}{
  morekeywords={
    void, int, int64, while, for
  },
  morecomment=[l]--
}
\lstdefinestyle{c}{
  language     = C,
  basicstyle   = \ttfamily,
  keywordstyle = [1]\color{keyword}\bfseries,
  keywordstyle = [2]\color{STD}\bfseries,
  commentstyle = \color{comment}
}
  
  


\title{${latexNoteJson.title}}
\author{AUTHOR_NAME}
\date{${LocalDate.now()}}

\begin{document}

\maketitle
\tableofcontents
\pagebreak

\section{Introduction}

\end{document}

        """.trimIndent()


        try {
            return transaction (connection){
                LatexNotesTable.insert {
                    it[LatexNotesTable.title] = latexNoteJson.title
                    it[LatexNotesTable.userId] = latexNoteJson.userId
                    it[LatexNotesTable.content] = initialLatex
                    it[LatexNotesTable.type] = latexNoteJson.type
                }[LatexNotesTable.id].toString().toInt()
            }
        }catch (e: Exception){
            println(e.message)
            return -1
        }
    }

    fun deleteLatexNote(noteId: Int){
        try {
            transaction (connection){
                LatexNotesTable.deleteWhere { LatexNotesTable.id eq noteId }
            }
        }catch (e: Exception){
            println(e.message)
        }
    }


    fun getLatexNotesTitles(userId: Int): List<InfoLatexJson>{
        val tmpList = mutableListOf<InfoLatexJson>()

        try {
            transaction(connection) {
                LatexNotesTable.selectAll().where {
                    LatexNotesTable.userId eq userId
                }.map {
                    tmpList.add(InfoLatexJson(it[LatexNotesTable.id].toString().toInt(), it[LatexNotesTable.title], it[LatexNotesTable.type]))
                }
            }
        }catch (e: Exception){
            println(e.message)
        }

        return tmpList
    }

    fun getLatexNoteContent(noteId: Int): String{
        try {
            return transaction (connection){
                LatexNotesTable.selectAll().where {
                    LatexNotesTable.id eq noteId
                }.firstOrNull()!![LatexNotesTable.content]
            }
        }catch (e: Exception){
            println(e.message)
            return ""
        }
    }

    fun updateLatexNote(notesJson: SaveNoteJson){
        transaction (connection){
            LatexNotesTable.update({ LatexNotesTable.id eq notesJson.noteId}){
                it[LatexNotesTable.content] = notesJson.content
            }
        }
    }


    fun getTodos(): List<TodoJson>{
        return transaction(DatabaseConfig.getGaiaConnection()){
            TodosTable.selectAll().map {
                TodoJson(
                    it[TodosTable.title],
                    it[TodosTable.description],
                    it[TodosTable.expirationDate],
                )
            }
        }
    }
}