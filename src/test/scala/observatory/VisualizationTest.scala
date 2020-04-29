package observatory

import org.junit.Assert._
import org.junit.Test

import scala.math.BigDecimal.RoundingMode

trait VisualizationTest extends MilestoneSuite {
  private val milestoneTest = namedMilestoneTest("raw data display", 2) _

  @Test def `greatCircleDistance function should return correct values`: Unit = {
    val Moscow = Location(55.751244, 37.618423)
    val London = Location(51.509865, -0.118092)
    val Kyiv = Location(50.434341, 30.527756)
    assert(math.floor(Visualization.greatCircleDistance(Moscow, London)) == 2499.0)
    assert(math.floor(Visualization.greatCircleDistance(Moscow, Kyiv)) == 756.0)
    assert(math.floor(Visualization.greatCircleDistance(London, Kyiv)) == 2133.0)
  }

  @Test def `predictTemperature function should return correct values`: Unit = {
    val data =
      Extraction
        .locateTemperatures(2015, "/stations.csv", "src/main/resources/2015_sample.csv")
        .map(row => (row._1, row._2, BigDecimal(row._3).setScale(1, RoundingMode.HALF_UP).toDouble))
    val avg = Extraction.locationYearlyAverageRecords(data)

    println(Visualization.predictTemperature(avg, Location(37.366, -78.443)))

  }


}

class predictTemperatureTest {
  @Test def `predictTemperature function should return correct values`: Unit = {
    val data =
      Extraction
        .locateTemperatures(2015, "/stations.csv", "/2015.csv")
    val avg = Extraction.locationYearlyAverageRecords(data)

    println(Visualization.predictTemperature(avg, Location(37.366, -78.443)))
  }
}
