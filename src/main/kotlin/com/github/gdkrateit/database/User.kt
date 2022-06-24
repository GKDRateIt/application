package com.github.gdkrateit.database

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
sealed interface UserModel {
    val email: String
    val hashedPassword: String
    val nickname: String
    val startYear: String
    val userGroup: String
}

object UserTable : IntIdTable(columnName = "u_user_id") {
    val email = varchar("u_user_email", 70).uniqueIndex()
    val hashedPassword = varchar("u_user_hashed_passwd", 256)
    val nickname = varchar("u_user_nickname", 20)
    val startYear = varchar("u_user_start_year", 10)
    val userGroup = varchar("u_user_group", 20)
}

class UserDao(id: EntityID<Int>) : IntEntity(id), UserModel {
    companion object : IntEntityClass<UserDao>(UserTable)

    override var email by UserTable.email
    override var hashedPassword by UserTable.hashedPassword
    override var nickname by UserTable.nickname
    override var startYear by UserTable.startYear
    override var userGroup by UserTable.userGroup
}