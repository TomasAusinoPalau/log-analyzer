package script.utils

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

/**
 * Ec to prevent overloading the default global thread pool
 * in case that log files contains a lot of registers.
 * */
object ScriptExecutionContext {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8))
}
