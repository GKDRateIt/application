package com.github.gkdrateit.service.review

import com.github.gkdrateit.database.*
import com.github.gkdrateit.service.ApiServer
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.util.*
import kotlin.properties.Delegates

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal abstract class TestBase {
    protected val apiServer = ApiServer()

    protected var testMemberUserId by Delegates.notNull<Int>()
    protected val testMemberUserEmail = "test@test.com"
    protected val testMemberUserRole = "Member"

    protected var testAdminUserId by Delegates.notNull<Int>()
    protected val testAdminUserEmail = "test_admin@test.com"
    protected val testAdminUserRole = "Admin"

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
        val tid = transaction { Teacher.all().first().id.value }

        if (transaction { User.find { Users.email eq testMemberUserEmail }.empty() }) {
            transaction {
                User.new {
                    email = testMemberUserEmail
                    hashedPassword = "???"
                    nickname = "???"
                    startYear = "???"
                    group = testMemberUserRole
                }
            }
        }
        testMemberUserId = transaction { User.find { Users.email eq testMemberUserEmail }.first().id.value }

        if (transaction { User.find { Users.email eq testAdminUserEmail }.empty() }) {
            transaction {
                User.new {
                    email = testAdminUserEmail
                    hashedPassword = "???"
                    nickname = "???"
                    startYear = "???"
                    group = testAdminUserRole
                }
            }
        }
        testAdminUserId = transaction { User.find { Users.email eq testAdminUserEmail }.first().id.value }

        if (transaction { Course.all().empty() }) {
            transaction {
                Course.new {
                    code = "B01GB001Y"
                    codeSeq = "A"
                    name = Base64.getEncoder().encodeToString("随便咯".toByteArray())
                    teacherId = tid
                    semester = "spring"
                    credit = BigDecimal.valueOf(1.5)
                    degree = 0
                    status = 1
                    category = "unknown"
                    submitUserId = testMemberUserId
                }
            }
        }
        if (transaction { User.all().empty() }) {
            transaction {
                User.new {
                    email = "test@ucas.ac.cn"
                    hashedPassword = "123456"
                    nickname = Base64.getEncoder().encodeToString("❤Aerith❤".toByteArray())
                    startYear = "2020"
                    group = "default"
                }
            }
        }
    }
}