import com.github.gdkrateit.database.Teacher
import com.github.gdkrateit.database.Teachers
import com.github.gdkrateit.service.ApiServer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

fun main(args: Array<String>) = runBlocking {
    val apiServerJob = launch { ApiServer().start() }
    val logger = LoggerFactory.getLogger("main")

    Database.connect(
        url = "jdbc:postgresql://localhost:5432/gkd_rate_it_test",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "112233445566"
    )

    transaction {
        addLogger(StdOutSqlLogger)
        // DSL api
        val noZk = Teachers.select {
            (Teachers.email eq """zk@ict.ac.cn""") and (Teachers.name eq "ZK")
        }.empty()
        if (noZk) {
            Teachers.insert {
                it[name] = "ZK"
                it[email] = "zk@ict.ac.cn"
            }
        }

        Teachers.select { Teachers.email match """_%@ict\.ac\.cn""" }.forEach {
            logger.info(it[Teachers.name])
        }

        // DAO api
        val noLp = Teacher.find {
            (Teachers.email eq """lp@ict.ac.cn""") and (Teachers.name eq "LP")
        }.empty()
        if (noLp) {
            Teacher.new {
                name = "LP"
                email = "lp@ict.ac.cn"
            }
        }

        Teacher.find { Teachers.email match """_%@ict\.ac\.cn""" }.forEach {
            logger.info(it.name)
        }
    }

    apiServerJob.join()
}