package observatory.unit

import observatory.{Extraction, Visualization}
import observatory.util.TemperatureToColor
import org.junit.Test

import scala.math.BigDecimal.RoundingMode

class VisualizeTest extends TemperatureToColor {
  @Test def `Visualization function should return correct values`: Unit = {

    val data =
      Extraction
        .locateTemperatures(2015, "/stations.csv", "/2015.csv")
        .map(row => (row._1, row._2, BigDecimal(row._3).setScale(1, RoundingMode.HALF_UP).toDouble))

    val res = Extraction.locationYearlyAverageRecords(data)

    Visualization.visualize(res, tToColor).output(new java.io.File("some-image.png"))

  }
}
