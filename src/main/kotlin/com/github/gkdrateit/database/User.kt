package com.github.gkdrateit.database

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

@Serializable
@SerialName("User")
private sealed interface UserModel {
    val email: String
    val hashedPassword: String
    val nickname: String
    val startYear: String
    val group: String
}

@OptIn(ExperimentalSerializationApi::class)
private class UserSerializer : KSerializer<User> {
    private val delegatedSerializer = UserModel.serializer()

    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("Teacher", delegatedSerializer.descriptor)

    override fun deserialize(decoder: Decoder): User {
        throw IllegalStateException("Database entity should not be deserialized.")
    }

    override fun serialize(encoder: Encoder, value: User) {
        encoder.encodeSerializableValue(delegatedSerializer, value)
    }
}

object Users : IntIdTable(columnName = "u_user_id") {
    val email = varchar("u_user_email", 70).uniqueIndex()
    val hashedPassword = varchar("u_user_hashed_passwd", 256)
    val nickname = varchar("u_user_nickname", 20)
    val startYear = varchar("u_user_start_year", 10)
    val group = varchar("u_user_group", 20)
}

@Serializable(with = UserSerializer::class)
class User(id: EntityID<Int>) : IntEntity(id), UserModel {
    companion object : IntEntityClass<User>(Users)

    override var email by Users.email
    override var hashedPassword by Users.hashedPassword
    override var nickname by Users.nickname
    override var startYear by Users.startYear
    override var group by Users.group
}