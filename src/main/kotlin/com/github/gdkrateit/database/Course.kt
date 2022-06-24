package com.github.gdkrateit.database

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import java.math.BigDecimal

@Serializable
sealed interface CourseModel {
    var code: String
    var codeSeq: String
    var name: String
    var teacherId: Int
    var semester: String
    var credit: BigDecimal
    var degree: Int
}


object CourseTable : IntIdTable(columnName = "c_course_id") {
    val code = char("c_course_name", 9)
    val codeSeq = varchar("c_course_code_seq", 5)
    val name = varchar("c_course_name", 30)
    val teacherId = integer("c_teacher_id").references(Teachers.id)
    val semester = varchar("c_semester", 10)
    val credit = decimal("c_credit", 4, 2)
    val degree = integer("c_degree")

    init {
        index(true, code, codeSeq, teacherId)
    }
}

class CourseDao(id: EntityID<Int>) : IntEntity(id), CourseModel {
    companion object : IntEntityClass<CourseDao>(CourseTable)

    override var code by CourseTable.code
    override var codeSeq by CourseTable.codeSeq
    override var name by CourseTable.name
    override var teacherId by CourseTable.teacherId
    override var semester by CourseTable.semester
    override var credit by CourseTable.credit
    override var degree by CourseTable.degree
}