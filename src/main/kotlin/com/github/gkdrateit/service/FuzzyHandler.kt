package com.github.gkdrateit.service

//import com.fasterxml.jackson.databind.ObjectMapper
//import com.github.gkdrateit.database.*
//import org.jetbrains.exposed.sql.transactions.transaction
//
//class FuzzyHandler : CrudApiBase() {
//    data class FuzzySearchResult(
//        val course: List<CourseModel>,
//        val teacher: List<TeacherModel>,
//    )
//
//    private val json = ObjectMapper()
//    private val asciiPattern = Regex("\\A\\p{ASCII}+\\z")
//
//    override val method: HttpMethod
//        get() = HttpMethod.POST
//
//    override fun handleCreate(param: Map<String, String>): ApiResponse<String> {
//        return notImplementedError()
//    }
//
//    override val path: String
//        get() = "/fuzzy"
//
//    override fun handleRead(param: Map<String, String>): ApiResponse<*> {
//        val keyWord = param["keyword"]
//        if (keyWord == null) {
//            return missingParamError("keyword")
//        }
//        val nonAsciiWord = mutableListOf<String>()
//        val asciiWord = mutableListOf<String>()
//        val keyWordListClazz = ArrayList<String>::class.java
//        json.readValue(keyWord,keyWordListClazz) .forEach {
//            if (it.matches(asciiPattern)) {
//                asciiWord.add(it)
//            } else {
//                nonAsciiWord.add(it)
//            }
//        }
//        val courseList = mutableListOf<CourseModel>()
//        val teacherList = mutableListOf<TeacherModel>()
//        asciiWord.forEach { word ->
//            transaction {
//                Course.find { Courses.code like "$word%" }.map { it.toModel() }
//            }.let {
//                courseList += it
//            }
//            val emailPrefix = word.substringBefore('@')
//            val emailPostfix = word.substringAfter('@')
//            var needNoPostFixMatch = false
//            transaction {
//                Teacher.find { Teachers.email like "$emailPrefix%@$emailPostfix" }.map { it.toModel() }
//            }.let {
//                teacherList += it
//                if (it.isEmpty()) {
//                    needNoPostFixMatch = true
//                }
//            }
//            if (needNoPostFixMatch) {
//                transaction {
//                    Teacher.find { Teachers.email like "$emailPrefix%" }.map { it.toModel() }
//                }.let {
//                    teacherList += it
//                }
//            }
//        }
//        nonAsciiWord.forEach { word ->
//            transaction {
//                Course.find { Courses.name like "$word%" }.map { it.toModel() }
//            }.let {
//                courseList += it
//            }
//            transaction {
//                Teacher.find { Teachers.name like "$word%" }.map { it.toModel() }
//            }.let {
//                teacherList += it
//            }
//        }
//        return successReply(FuzzySearchResult(courseList, teacherList))
//    }
//
//    override fun handleUpdate(param: Map<String, String>): ApiResponse<String> {
//        return notImplementedError()
//    }
//
//    override fun handleDelete(param: Map<String, String>): ApiResponse<String> {
//        return notImplementedError()
//    }
//}