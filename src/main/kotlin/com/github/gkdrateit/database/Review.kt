package com.github.gkdrateit.database

import com.github.gkdrateit.util.LocalDateTimeSerializer
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
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

@Serializable
@SerialName("Review")
private sealed interface ReviewModel {
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
    val myGrade: String?
    val myMajor: Int?
}

@OptIn(ExperimentalSerializationApi::class)
private class ReviewSerializer : KSerializer<Review> {
    private val delegatedSerializer = ReviewModel.serializer()

    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("Teacher", delegatedSerializer.descriptor)

    override fun deserialize(decoder: Decoder): Review {
        throw IllegalStateException("Database entity should not be deserialized.")
    }

    override fun serialize(encoder: Encoder, value: Review) {
        encoder.encodeSerializableValue(delegatedSerializer, value)
    }
}

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

@Serializable(with = ReviewSerializer::class)
class Review(id: EntityID<Int>) : IntEntity(id), ReviewModel {
    companion object : IntEntityClass<Review>(Reviews)

    override var courseId by Reviews.courseId
    override var userId by Reviews.userId
    override var createTime by Reviews.createTime
    override var lastUpdateTime by Reviews.lastUpdateTime
    override var overallRecommendation by Reviews.overallRecommendation
    override var quality by Reviews.quality
    override var difficulty by Reviews.difficulty
    override var workload by Reviews.workload
    override var commentText by Reviews.commentText
    override var myGrade by Reviews.myGrade
    override var myMajor by Reviews.myMajor
}