import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import timber.log.Timber
import kotlin.coroutines.CoroutineContext



// message types that the actor will handle
sealed class Action
data class Spin(val id : Int) : Action()
data class Done(val ack : CompletableDeferred<Boolean>) : Action()


class MailBox : CoroutineScope by MainScope(){


    val actor = actor<Action>(Dispatchers.Default, 0) {
        consumeEach { action ->
            when(action){
                is Spin -> spin(action.id)
            }
        }
    }

    private fun spin(value: Int) {
        val startInMillSeconds = System.currentTimeMillis()
        while (System.currentTimeMillis() - startInMillSeconds < 10){}
        Timber.d("[thread=${Thread.currentThread().name}] [$value]")
    }

}


fun main() {
    val mailBox = MailBox()

    val start = System.currentTimeMillis()

    (1..1000).map { Spin(it) }
            .forEach { spin: Spin ->
                CoroutineScope(Dispatchers.Main).launch {
                    mailBox.actor.send(spin)

                    val duration = System.currentTimeMillis() - start
                    Timber.d("[thread=${Thread.currentThread().name}] [${spin.id}]"
                    + "time = $duration")


                }
            }

    Timber.d(
        "time = ${System.currentTimeMillis() - start}")
}