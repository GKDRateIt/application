package com.github.gdkrateit.database

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

@Serializable
data class TeacherModel(
    val name: String,
    val email: String,
)

object Teachers : IntIdTable(columnName = "t_teacher_id") {
    val name: Column<String> = varchar("c_teacher_name", 50)
    val email: Column<String> = varchar("c_teacher_email", 70)
}

// DAO class
class Teacher(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Teacher>(Teachers)

    var name by Teachers.name
    var email by Teachers.email

    fun toModel(): TeacherModel {
        return TeacherModel(name, email)
    }
}