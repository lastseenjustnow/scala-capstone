package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.util.TemperatureToColor
import org.junit.Assert._
import org.junit.Test

trait VisualizationTest extends MilestoneSuite with TemperatureToColor {
  private val milestoneTest = namedMilestoneTest("raw data display", 2) _

  @Test def `greatCircleDistance function should return correct values`: Unit = {
    val Moscow = Location(55.751244, 37.618423)
    val London = Location(51.509865, -0.118092)
    val Kyiv = Location(50.434341, 30.527756)
    assert(math.floor(Visualization.greatCircleDistance(Moscow, London)) == 2499.0)
    assert(math.floor(Visualization.greatCircleDistance(Moscow, Kyiv)) == 756.0)
    assert(math.floor(Visualization.greatCircleDistance(London, Kyiv)) == 2133.0)
  }

  @Test def `interpolateColor`: Unit = {
    lazy val res1 = Visualization.interpolateColor(tToColor, 46.0)
    lazy val expRes1 = Color(255, 128, 128)

    lazy val res2 = Visualization.interpolateColor(tToColor, 70.0)
    lazy val expRes2 = Color(255, 255, 255)

    lazy val res3 = Visualization.interpolateColor(tToColor, -70.0)
    lazy val expRes3 = Color(0, 0, 0)

    lazy val res4 = Visualization.interpolateColor(tToColor, 16.0)
    lazy val expRes4 = Color(255, 204, 0)

    assert(res1 == expRes1)
    assert(res2 == expRes2)
    assert(res3 == expRes3)
    assert(res4 == expRes4)
  }

  @Test def `interpolateColor with predefined scale`: Unit = {
    val scale = List((0.0,Color(255,0,0)), (1.52587890625E-5,Color(0,0,255)))
    val res = Visualization.interpolateColor(scale, 0.0)
    val expRes = Color(255,0,0)

    assert(res == expRes)
  }
}
