package script.model

case class UserMetrics(
  userId: String,
  pages: Int,
  sessions: Int,
  maxDuration: Long,
  minDuration: Long
)
