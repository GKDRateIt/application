import com.github.gkdrateit.service.ApiServer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

fun main(args: Array<String>) = runBlocking {
    val apiServerJob = launch { ApiServer().start() }
    val logger = LoggerFactory.getLogger("main")



    apiServerJob.join()
}