package observatory.unit

import observatory.{Color, Visualization}
import org.junit.Test

class InterpolateColorTest {
  @Test def `interpolateColor with predefined scale`: Unit = {
    val scale = List((0.0, Color(255, 0, 0)), (1.52587890625E-5, Color(0, 0, 255)))
    val res = Visualization.interpolateColor(scale, 0.0)
    val expRes = Color(255, 0, 0)

    assert(res == expRes)
  }
}
