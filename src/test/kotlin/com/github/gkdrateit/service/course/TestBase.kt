package com.github.gkdrateit.service.course

import com.github.gkdrateit.database.*
import com.github.gkdrateit.service.ApiServer
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import kotlin.properties.Delegates

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal abstract class TestBase {
    protected val apiServer = ApiServer()

    protected var testUserId by Delegates.notNull<Int>()
    protected val testUserEmail = "test@test.com"
    protected val testUserRole = "Member"

    @BeforeAll
    fun setup() {
        TestDbAdapter.setup()
        prepareTable()
    }

    private fun prepareTable() {
        if (transaction { Teacher.all().empty() }) {
            transaction {
                Teacher.new {
                    name = "TestTeacher"
                    email = "test_teacher@ucas.ac.cn"
                }
            }
        }
        if (transaction { User.all().empty() }) {
            transaction {
                User.new {
                    email = testUserEmail
                    hashedPassword = "???"
                    nickname = "???"
                    startYear = "???"
                    group = testUserEmail
                }
            }
        }
        testUserId = transaction { User.find { Users.email eq testUserEmail }.first().id.value }
        val qTeacherId = transaction { Teacher.all().first().id.value }
        // Create some course entries
        if (transaction { Course.find { Courses.name like "测试课程%" }.empty() }) {
            transaction {
                Course.new {
                    code = "000000000"
                    codeSeq = "A"
                    name = "测试课程-1"
                    teacherId = qTeacherId
                    semester = "spring"
                    credit = BigDecimal.valueOf(1.5)
                    degree = 0
                    status = 1
                    category = "unknown"
                    submitUserId = testUserId
                }
                Course.new {
                    code = "000000001"
                    codeSeq = "B"
                    name = "测试课程-2"
                    teacherId = qTeacherId
                    semester = "spring"
                    credit = BigDecimal.valueOf(1.5)
                    degree = 0
                    status = 1
                    category = "unknown"
                    submitUserId = testUserId
                }
            }
        }
    }
}