package script

import database.QueryManager
import database.tables.TodosTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import server.JSONmodels.TodoJson

class TodoManager(val connection: Database){
    fun addTodo(todoJson: TodoJson){
        try{
            transaction(connection) {
                TodosTable.insert {
                    it[TodosTable.title] = todoJson.title
                    it[TodosTable.description] = todoJson.description
                    it[TodosTable.expirationDate] = todoJson.expirationDate
                    it[TodosTable.type] = todoJson.type
                }
            }
        }catch (e: Exception){
            throw e
        }
    }

    fun removeTodo(todoId: Int){
        try{
            transaction(connection) {
                TodosTable.deleteWhere {
                    TodosTable.id eq todoId
                }
            }
        }catch (e: Exception){
            throw e
        }
    }

    fun getTodos(): List<TodoJson>{
        return transaction(connection){
            TodosTable.selectAll().map {
                TodoJson(
                    it[TodosTable.title],
                    it[TodosTable.description],
                    it[TodosTable.expirationDate],
                    it[TodosTable.id].value,
                    it[TodosTable.type]
                )
            }
        }
    }

    fun getOrgTodos(): String{
        var workTodos = listOf<TodoJson>()
        var uniTodos = listOf<TodoJson>()
        var otherTodos = listOf<TodoJson>()

        transaction(connection) {
            workTodos = TodosTable.selectAll().where { TodosTable.type eq "Work" }.map {
                TodoJson(
                    it[TodosTable.title],
                    it[TodosTable.description],
                    it[TodosTable.expirationDate],
                    it[TodosTable.id].value,
                    it[TodosTable.type]
                )
            }

            uniTodos = TodosTable.selectAll().where { TodosTable.type eq "University" }.map {
                TodoJson(
                    it[TodosTable.title],
                    it[TodosTable.description],
                    it[TodosTable.expirationDate],
                    it[TodosTable.id].value,
                    it[TodosTable.type]
                )
            }

            otherTodos = TodosTable.selectAll().where { TodosTable.type eq "Other" }.map {
                TodoJson(
                    it[TodosTable.title],
                    it[TodosTable.description],
                    it[TodosTable.expirationDate],
                    it[TodosTable.id].value,
                    it[TodosTable.type]
                )
            }
        }
        var uniTodoString = ""
        var workTodoString = ""
        var otherTodoString = ""

        uniTodos.map { todo ->
            uniTodoString += "** [${todo.id}] ${todo.title} - ${todo.description} - ${todo.expirationDate}\n"
        }
        workTodos.map { todo ->
            workTodoString += "** [${todo.id}] ${todo.title} - ${todo.description} - ${todo.expirationDate}\n"
        }
        otherTodos.map { todo ->
            otherTodoString += "** [${todo.id}] ${todo.title} - ${todo.description} - ${todo.expirationDate}\n"
        }

       return  """
#+TITLE: Todos
* Lavoro
$workTodoString

* Università 
$uniTodoString

* Altro
$otherTodoString
        """.trimIndent()
    }



}