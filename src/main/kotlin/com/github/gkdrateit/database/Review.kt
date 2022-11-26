package com.github.gkdrateit.database


import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

//@Serializable
data class ReviewModel(
    val reviewId: Int,
    val courseId: Int,
    val userId: Int,
//    @Serializable(with = LocalDateTimeSerializer::class)
    val createTime: LocalDateTime,
//    @Serializable(with = LocalDateTimeSerializer::class)
    val lastUpdateTime: LocalDateTime,
    val overallRecommendation: Int,
    val quality: Int,
    val difficulty: Int,
    val workload: Int,
    val commentText: String,
    val myGrade: String?,
    val myMajor: Int?,
    val userName: String?,
)

object Reviews : IntIdTable(columnName = "r_review_id") {
    val courseId = integer("r_course_id").references(Courses.id)
    val userId = integer("r_user_id").references(Users.id)
    val createTime = datetime("r_create_time")
    val lastUpdateTime = datetime("r_last_update_time")
    val overallRecommendation = integer("r_overall_rec")
    val quality = integer("r_rate_quality")
    val difficulty = integer("r_rate_difficulty")
    val workload = integer("r_rate_workload")
    val commentText = text("r_comment_text")
    val myGrade = varchar("r_my_grade", 10).nullable()
    val myMajor = integer("r_my_major").nullable()
}

class Review(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Review>(Reviews)

    var courseId by Reviews.courseId
    var userId by Reviews.userId
    var createTime by Reviews.createTime
    var lastUpdateTime by Reviews.lastUpdateTime
    var overallRecommendation by Reviews.overallRecommendation
    var quality by Reviews.quality
    var difficulty by Reviews.difficulty
    var workload by Reviews.workload
    var commentText by Reviews.commentText
    var myGrade by Reviews.myGrade
    var myMajor by Reviews.myMajor

    fun toModel(nickname: String?): ReviewModel {
        return ReviewModel(
            id.value,
            courseId,
            userId,
            createTime,
            lastUpdateTime,
            overallRecommendation,
            quality,
            difficulty,
            workload,
            commentText,
            myGrade,
            myMajor,
            nickname
        )
    }
}