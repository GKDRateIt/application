package com.github.gkdrateit.database

import com.github.gkdrateit.util.BigDecimallSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import java.math.BigDecimal

@Serializable
data class CourseModel(
    val courseId: Int,
    val code: String,
    val codeSeq: String?,
    val name: String,
    val teacherId: Int,
    val semester: String,

    @Serializable(with = BigDecimallSerializer::class)
    val credit: BigDecimal,
    val degree: Int,
)

object Courses : IntIdTable(columnName = "c_course_id") {
    val code = char("c_course_code", 9)
    val codeSeq = varchar("c_course_code_seq", 5).nullable()
    val name = varchar("c_course_name", 30)
    val teacherId = integer("c_teacher_id").references(Teachers.id)
    val semester = varchar("c_semester", 10)
    val credit = decimal("c_credit", 4, 2)
    val degree = integer("c_degree")

    init {
        index(true, code, codeSeq, teacherId)
    }
}

class Course(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Course>(Courses)

    var code by Courses.code
    var codeSeq by Courses.codeSeq
    var name by Courses.name
    var teacherId by Courses.teacherId
    var semester by Courses.semester
    var credit by Courses.credit
    var degree by Courses.degree

    fun toModel(): CourseModel {
        return CourseModel(id.value, code, codeSeq, name, teacherId, semester, credit, degree)
    }
}