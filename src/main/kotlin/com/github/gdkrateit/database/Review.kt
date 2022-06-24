package com.github.gdkrateit.database

import com.github.gdkrateit.util.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

@Serializable
sealed interface ReviewModel {
    val courseId: Int
    val userId: Int
    @Serializable(with = LocalDateTimeSerializer::class)
    val createTime: LocalDateTime
    @Serializable(with = LocalDateTimeSerializer::class)
    val lastUpdateTime: LocalDateTime
    val overallRecommendation: Int
    val quality: Int
    val difficulty: Int
    val workload: Int
    val commentText: String
    val myGrade: String
    val myMajor: Int
}

object ReviewTable : IntIdTable(columnName = "r_review_id") {
    val courseId = integer("r_course_id").references(CourseTable.id)
    val userId = integer("r_user_id").references(UserTable.id)
    val createTime = datetime("r_create_time")
    val lastUpdateTime = datetime("r_last_update_time")
    val overallRecommendation = integer("r_overall_rec")
    val quality = integer("r_rate_quality")
    val difficulty = integer("r_rate_difficulty")
    val workload = integer("r_rate_workload")
    val commentText = text("r_comment_text")
    val myGrade = varchar("r_my_grade", 10)
    val myMajor = integer("r_my_major")
}

class ReviewDao(id: EntityID<Int>) : IntEntity(id), ReviewModel {
    companion object : IntEntityClass<ReviewDao>(ReviewTable)

    override var courseId by ReviewTable.courseId
    override var userId by ReviewTable.userId
    override var createTime by ReviewTable.createTime
    override var lastUpdateTime by ReviewTable.lastUpdateTime
    override var overallRecommendation by ReviewTable.overallRecommendation
    override var quality by ReviewTable.quality
    override var difficulty by ReviewTable.difficulty
    override var workload by ReviewTable.workload
    override var commentText by ReviewTable.commentText
    override var myGrade by ReviewTable.myGrade
    override var myMajor by ReviewTable.myMajor
}