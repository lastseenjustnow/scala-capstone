package observatory

import com.sksamuel.scrimage.{Image, Pixel}

import math.{Pi, acos, cos, pow, sin, round, min, max}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * 2nd milestone: basic visualization
  */
object Visualization extends VisualizationInterface {

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

    val temps = temperatures.par

    val filtered = temps
      .map { case (l, t) => (greatCircleDistance(l, location), t) }
      .filter(_._1 < 1)

    val reduceWeights = (v1: (Double, Double), v2: (Double, Double)) => (v1._1 + v2._1, v1._2 + v2._2)

    lazy val reduce =
      temps
        .map { case (loc: Location, temp: Temperature) => (distanceWeight(loc, location), distanceWeight(loc, location) * temp) }
        .reduce(reduceWeights)

    if (filtered.nonEmpty) {
      filtered.minBy(_._1)._2
    } else reduce._2 / reduce._1

  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {

    val pointsSorted = points.toSeq.sortBy(_._1)

    val leftEnd  = (Double.NegativeInfinity, pointsSorted.head._2)
    val rightEnd = (Double.PositiveInfinity, pointsSorted.last._2)
    val range    = leftEnd +: pointsSorted :+ rightEnd

    val (t1, c1) = range.sliding(2).toSeq.find(t => t.head._1 <= value && t.last._1 >= value).get.head
    val (t2, c2) = range.sliding(2).toSeq.find(t => t.head._1 <= value && t.last._1 >= value).get.last

    val fraction =
      if (math.abs(value - t1) == Double.PositiveInfinity) 1
      else (value - t1) / (t2 - t1)

    Color(
      min(max(round(c1.red   * (1 - fraction)) + round(c2.red   * fraction), 0), 255) toInt,
      min(max(round(c1.green * (1 - fraction)) + round(c2.green * fraction), 0), 255) toInt,
      min(max(round(c1.blue  * (1 - fraction)) + round(c2.blue  * fraction), 0), 255) toInt
    )
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {

    val temps = Future(
      for (lon <- 90 until -90 by -1; lat <- -180 until 180)
        yield {
          val c = interpolateColor(colors, predictTemperature(temperatures, Location(lon, lat)))
          Pixel(c.red, c.green, c.blue, 255)
        }
    )

    Image.apply(360, 180, Await.result(temps, 20 seconds).toArray)

  }

}

