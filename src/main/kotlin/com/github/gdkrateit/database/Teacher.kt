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
import org.jetbrains.exposed.sql.Column

@Serializable
@SerialName("Teacher")
private sealed interface TeacherModel {
    var name: String
    var email: String?
}

@OptIn(ExperimentalSerializationApi::class)
private class TeacherSerializer : KSerializer<Teacher> {
    private val delegatedSerializer = TeacherModel.serializer()

    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("Teacher", delegatedSerializer.descriptor)

    override fun deserialize(decoder: Decoder): Teacher {
        throw IllegalStateException("Database entity should not be deserialized.")
    }

    override fun serialize(encoder: Encoder, value: Teacher) {
        encoder.encodeSerializableValue(delegatedSerializer, value)
    }
}

object Teachers : IntIdTable(columnName = "t_teacher_id") {
    val name: Column<String> = varchar("c_teacher_name", 50)
    val email: Column<String?> = varchar("c_teacher_email", 70).nullable()
}

// DAO class
@Serializable(with = TeacherSerializer::class)
class Teacher(id: EntityID<Int>) : IntEntity(id), TeacherModel {
    companion object : IntEntityClass<Teacher>(Teachers)

    override var name by Teachers.name
    override var email by Teachers.email
}