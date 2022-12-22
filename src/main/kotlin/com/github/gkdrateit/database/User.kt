package com.github.gkdrateit.database


import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

//@Serializable
data class UserModel(
    val userId: Int,
    val email: String,
    val hashedPassword: String,
    val nickname: String,
    val startYear: String,
    val group: String,
)

object Users : IntIdTable(columnName = "u_user_id") {
    val email = varchar("u_user_email", 70).uniqueIndex()
    val hashedPassword = varchar("u_user_hashed_passwd", 256)
    val nickname = varchar("u_user_nickname", 20)
    val startYear = varchar("u_user_start_year", 10)
    val group = varchar("u_user_group", 20)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var email by Users.email
    var hashedPassword by Users.hashedPassword
    var nickname by Users.nickname
    var startYear by Users.startYear
    var group by Users.group

    fun toModel(): UserModel {
        return UserModel(id.value, email, hashedPassword, nickname, startYear, group)
    }
}