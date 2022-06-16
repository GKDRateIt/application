import com.github.gdkrateit.service.ApiServer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {
    val apiServerJob = launch { ApiServer().start() }
    apiServerJob.join()
}