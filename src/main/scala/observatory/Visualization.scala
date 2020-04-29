package observatory

import com.sksamuel.scrimage.{Image, Pixel}

import math.{Pi, acos, cos, pow, sin, abs}
import org.apache.spark.sql.SparkSession

/**
  * 2nd milestone: basic visualization
  */
object Visualization extends VisualizationInterface {

  val spark: SparkSession =
    SparkSession
      .builder()
      .appName("Visualization")
      .master("local")
      .getOrCreate()

  def greatCircleDistance(p1: Location, p2: Location): Double = {
    val r = 6371.0088 // earth approx radius
    val φ1 = p1.lat / 180 * Pi
    val φ2 = p2.lat / 180 * Pi
    val λ1 = p1.lon / 180 * Pi
    val λ2 = p2.lon / 180 * Pi
    val Δλ = λ1 - λ2

    val Δσ = if (φ1 == φ2 && λ1 == λ2) 0
    else if (φ1 == -φ2 && λ1 == -λ2) Pi
    else acos(sin(φ1) * sin(φ2) + cos(φ1) * cos(φ2) * cos(Δλ))

    r * Δσ
  }

  def distanceWeight(p1: Location, p2: Location): Double = {
    val p = 2
    1 / pow(greatCircleDistance(p1, p2), p)
  }

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {

    val df = spark.sparkContext.parallelize(temperatures.toSeq)

    val dfDistance = {
      val out = df
        .map { case (l, t) => (greatCircleDistance(l, location), t) }
        .filter(_._1 < 1)
        .sortByKey(ascending = true)
      import spark.implicits._
      out.toDF.show(false)
      out
    }

    lazy val map = {
      println("I'm in map!")
      df.map { case (loc, temp) => (distanceWeight(loc, location), distanceWeight(loc, location) * temp) }
    }

    lazy val reduce = {
      println("I'm in reduce!")
      map.reduce { case ((wloc1, wtemp1), (wloc2, wtemp2)) => (wloc1 + wloc2, wtemp1 + wtemp2) }
    }

    if (dfDistance.count != 0) dfDistance.first._2 else reduce._2 / reduce._1

  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {

    val df = spark.sparkContext.parallelize(points.toSeq)

    val pair = df
      .map{ case (temp, color) => (abs(temp - value), color)}
      .sortByKey(ascending = true)
      .take(2)

    def interpColor(c1: Color, c2: Color): Color =
      Color(c1.red + c2.red / 2, c1.green + c2.green / 2, c1.blue + c2.blue / 2)

    if (pair.head._1 == 0) pair.head._2 else interpColor(pair.head._2, pair.last._2)

  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {

    val tempDf = spark.sparkContext.parallelize(temperatures.toSeq)

    val pixels = tempDf
      .sortBy(_._1.lat, ascending = false)
      .sortBy(_._1.lon, ascending = true)
      .mapValues(temp => interpolateColor(colors, temp))
      .values
      .map( col => Pixel(col.red, col.green, col.blue, 0))
      .collect()

    Image.apply(360, 180, pixels)

  }

}

