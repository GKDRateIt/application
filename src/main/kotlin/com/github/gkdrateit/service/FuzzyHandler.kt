package com.github.gkdrateit.service

import com.github.gkdrateit.database.*
import io.javalin.http.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction

class FuzzyHandler : ApiBase() {
    data class FuzzySearchResult(
        val course: List<CourseModel>,
        val teacher: List<TeacherModel>,
    )

    private val json = Json
    private val asciiPattern = Regex("\\A\\p{ASCII}+\\z")

    override val method: HttpMethod
        get() = HttpMethod.POST
    override val path: String
        get() = "/fuzzy"

    override fun handle(ctx: Context) {
        val keyWord = ctx.formParam("keyword")
        if (keyWord == null) {
            ctx.missingParamError("keyword")
            return
        }
        val nonAsciiWord = mutableListOf<String>()
        val asciiWord = mutableListOf<String>()
        json.decodeFromString<List<String>>(keyWord).forEach {
            if (it.matches(asciiPattern)) {
                asciiWord.add(it)
            } else {
                nonAsciiWord.add(it)
            }
        }
        val courseList = mutableListOf<CourseModel>()
        val teacherList = mutableListOf<TeacherModel>()
        asciiWord.forEach { word ->
            transaction {
                Course.find { Courses.code like "$word%" }.map { it.toModel() }
            }.let {
                courseList += it
            }
            val emailPrefix = word.substringBefore('@')
            val emailPostfix = word.substringAfter('@')
            var needNoPostFixMatch = false
            transaction {
                Teacher.find { Teachers.email like "$emailPrefix%@$emailPostfix" }.map { it.toModel() }
            }.let {
                teacherList += it
                if (it.isEmpty()) {
                    needNoPostFixMatch = true
                }
            }
            if (needNoPostFixMatch) {
                transaction {
                    Teacher.find { Teachers.email like "$emailPrefix%" }.map { it.toModel() }
                }.let {
                    teacherList += it
                }
            }
        }
        nonAsciiWord.forEach { word ->
            transaction {
                Course.find { Courses.name like "$word%" }.map { it.toModel() }
            }.let {
                courseList += it
            }
            transaction {
                Teacher.find { Teachers.name like "$word%" }.map { it.toModel() }
            }.let {
                teacherList += it
            }
        }
        ctx.successReply(FuzzySearchResult(courseList, teacherList))
    }
}