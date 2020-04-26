package observatory

import java.time.LocalDate

import observatory.struct.{Stations, Temperatures}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.io.Source
import scala.reflect.ClassTag

/**
  * 1st milestone: data extraction
  */
object Extraction extends ExtractionInterface {

  val spark: SparkSession =
    SparkSession
      .builder()
      .appName("Extraction")
      .master("local")
      .getOrCreate()

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  val parseStations: Seq[String] => Stations =
    x => Stations(strToOption(x.head), strToOption(x(1)), toDouble(x(2)), toDouble(x(3)))
  val parseTemperatures: Seq[String] => Temperatures =
    x => Temperatures(strToOption(x.head), strToOption(x(1)), x(2).toInt, x(3).toInt, x(4).toDouble)

  def sourceToRDD[T: ClassTag]
  (path: String,
   sep: String = ",",
   header: Boolean = false)
  (parse: Seq[String] => T): RDD[T] = {
    val datalines = Source.fromInputStream(getClass.getResourceAsStream(path), "utf-8").getLines().toSeq
    spark.sparkContext.parallelize(datalines.map(str => parse(str.split(sep, -1))))
  }

  private def toDouble(s: String): Option[Double] = {
    try {
      Some(s.trim.toDouble)
    } catch {
      case e: Exception => None
    }
  }

  private def strToOption(s: String): Option[String] = if (s.isEmpty) None else Some(s)

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {

    val stations: RDD[((Option[String], Option[String]), Location)] =
      sourceToRDD[Stations](stationsFile)(parseStations)
        .filter(x => x.Latitude.nonEmpty & x.Longitude.nonEmpty)
        .map(station => ((station.STN, station.WBAN), Location(station.Latitude.get, station.Longitude.get)))

    val temperatures: RDD[((Option[String], Option[String]), (LocalDate, Temperature))] =
      sourceToRDD[Temperatures](temperaturesFile)(parseTemperatures)
        .map(temp => ((temp.STN, temp.WBAN), (LocalDate.of(year, temp.month, temp.day), (temp.tFahrenheit - 32) / 1.8)))

    stations.join(temperatures).mapValues(x => (x._2._1, x._1, x._2._2)).values.collect()

  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    spark.sparkContext.parallelize(records.toSeq)
      .map(rec => (rec._2, (rec._3, 1)))
      .reduceByKey((v1, v2) => (v1._1 + v2._1, v1._2 + v2._2))
      .mapValues(tup => tup._1 / tup._2)
      .collect()
  }

}