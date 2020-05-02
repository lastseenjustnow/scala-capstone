package observatory.unit

import observatory.{Location, Visualization}
import org.junit.Test

class PredictTemperatureTest {
  @Test def `predictTemperature function should return correct values`: Unit = {

    val knownTemps = Seq(
      (Location(37.358, -78.438), 1.0),
      (Location(37.35, -78.433), 27.3)
    )

    println(Visualization.predictTemperature(knownTemps, Location(38.358, -77.438)))
    println(Visualization.predictTemperature(knownTemps, Location(37.359, -78.437)))

  }
}