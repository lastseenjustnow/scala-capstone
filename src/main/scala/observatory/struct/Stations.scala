package observatory.struct

case class Stations(
                     STN: Option[String],
                     WBAN: Option[String],
                     Latitude: Option[Double],
                     Longitude: Option[Double]
                   )
