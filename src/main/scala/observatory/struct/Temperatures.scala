package observatory.struct

case class Temperatures(
                         STN: Option[String],
                         WBAN: Option[String],
                         month: Int,
                         day: Int,
                         tFahrenheit: Double
                       )
