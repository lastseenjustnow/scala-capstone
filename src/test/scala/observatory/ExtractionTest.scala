package observatory

import java.time.LocalDate

import observatory.Extraction.{parseStations, sourceToRDD}
import observatory.struct.Stations
import org.apache.spark.rdd.RDD
import org.junit.Test
import org.apache.spark.sql.functions.col

import scala.math.BigDecimal.RoundingMode

trait ExtractionTest extends MilestoneSuite {
  private val milestoneTest = namedMilestoneTest("data extraction", 1) _

  @Test def `Tables have to contain all types of values in columns`: Unit = {
    val stations: RDD[Stations] = sourceToRDD[Stations]("/stations.csv")(parseStations)
    import Extraction.spark.implicits._
    stations.toDF.show(false)
    assert(stations.toDF.where(col("WBan").isNull).count > 0, "Stations data might return null values!")
    assert(stations.toDF.where(col("Latitude") < 0).count > 0, "Stations data might return negative values!")
  }

  @Test def `Function locateTemperatures should correctly convert test data to appropriate classes`: Unit = {

    val res =
      Extraction
        .locateTemperatures(2015, "/stations.csv", "/2015.csv")
        .map(row => (row._1, row._2, BigDecimal(row._3).setScale(1, RoundingMode.HALF_UP).toDouble))

    val expRes = Seq(
      (LocalDate.of(2015, 8, 11), Location(37.35, -78.433), 27.3),
      (LocalDate.of(2015, 12, 6), Location(37.358, -78.438), 0.0),
      (LocalDate.of(2015, 1, 29), Location(37.358, -78.438), 2.0)
    )

    assert(res == expRes)
  }

  @Test def `Function locationYearlyAverageRecords should correctly aggregate test data`: Unit = {

    val data =
      Extraction
        .locateTemperatures(2015, "/stations.csv", "/2015.csv")
        .map(row => (row._1, row._2, BigDecimal(row._3).setScale(1, RoundingMode.HALF_UP).toDouble))

    val res = Extraction.locationYearlyAverageRecords(data)

    val expRes = Seq(
      (Location(37.358, -78.438), 1.0),
      (Location(37.35, -78.433), 27.3)
    )

    assert(res == expRes)
  }

}
