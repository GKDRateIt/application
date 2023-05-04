package com.github.gkdrateit.database

import com.github.gkdrateit.config.RateItConfig
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import kotlin.concurrent.schedule

interface ICourseSlimModel {
    val courseId: Int
    val code: String
    val codeSeq: String?
    val name: String
    val teacherId: Int
    val semester: String
    val credit: BigDecimal
    val degree: Int
    val category: String
    val status: Int
    val submitUserId: Int
}

data class CourseSlimModel(
    override val courseId: Int,
    override val code: String,
    override val codeSeq: String?,
    override val name: String,
    override val teacherId: Int,
    override val semester: String,
    override val credit: BigDecimal,
    override val degree: Int,
    override val category: String,
    override val status: Int,
    override val submitUserId: Int
) : ICourseSlimModel {
    fun addRatingInfo(): CourseModel {
        val res = "SELECT * FROM AvgRating WHERE r_course_id = $courseId".execAndMap {
            CourseModel(
                this,
                it.getInt("r_avg_overall_rec"),
                it.getInt("r_avg_rate_difficulty"),
                it.getInt("r_avg_rate_quality"),
                it.getInt("r_avg_rate_workload")
            )
        }
        if (res.isEmpty()) {
            // No review yet!
            return CourseModel(
                this, 0, 0, 0, 0
            )
        }
        return res[0]
    }
}

data class CourseModel(
    private val slimModel: CourseSlimModel,
    val overallRecommendation: Int,
    val difficulty: Int,
    val quality: Int,
    val workload: Int,
) : ICourseSlimModel by slimModel

object Courses : IntIdTable(columnName = "c_course_id") {
    val code = char("c_course_code", 9)
    val codeSeq = varchar("c_course_code_seq", 5).nullable()
    val name = varchar("c_course_name", 30)
    val teacherId = integer("c_teacher_id").references(Teachers.id)
    val semester = varchar("c_semester", 10)
    val credit = decimal("c_credit", 4, 2)
    val degree = integer("c_degree")
    val category = varchar("c_category", 15)
    val status = integer("c_status")
    val submitUserId = integer("c_submit_user_id").references(Users.id)

    init {
        index(true, code, codeSeq, teacherId)
    }

    private val refreshTask = dbTimer.schedule(0, 1000 * 60 * 10) {
        if (RateItConfig.refreshAvgView) {
            transaction {
                "REFRESH MATERIALIZED VIEW AvgRating".exec()
            }
        }
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
    var category by Courses.category
    var status by Courses.status
    var submitUserId by Courses.submitUserId

    fun toModel(): CourseModel {
        return CourseSlimModel(
            id.value,
            code,
            codeSeq,
            name,
            teacherId,
            semester,
            credit,
            degree,
            category,
            status,
            submitUserId
        ).addRatingInfo()
    }
}