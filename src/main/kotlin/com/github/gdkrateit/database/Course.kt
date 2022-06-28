package com.github.gdkrateit.database

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import java.math.BigDecimal

@Serializable
@SerialName("Course")
private sealed interface CourseModel {
    var code: String
    var codeSeq: String?
    var name: String
    var teacherId: Int
    var semester: String
    var credit: BigDecimal
    var degree: Int
}

@OptIn(ExperimentalSerializationApi::class)
private class CourseSerializer : KSerializer<Course> {
    private val delegatedSerializer = CourseModel.serializer()

    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("Teacher", delegatedSerializer.descriptor)

    override fun deserialize(decoder: Decoder): Course {
        throw IllegalStateException("Database entity should not be deserialized.")
    }

    override fun serialize(encoder: Encoder, value: Course) {
        encoder.encodeSerializableValue(delegatedSerializer, value)
    }
}


object Courses : IntIdTable(columnName = "c_course_id") {
    val code = char("c_course_name", 9)
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

@Serializable(with = CourseSerializer::class)
class Course(id: EntityID<Int>) : IntEntity(id), CourseModel {
    companion object : IntEntityClass<Course>(Courses)

    override var code by Courses.code
    override var codeSeq by Courses.codeSeq
    override var name by Courses.name
    override var teacherId by Courses.teacherId
    override var semester by Courses.semester
    override var credit by Courses.credit
    override var degree by Courses.degree
}