package observatory.unit

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.util.TemperatureToColor
import org.junit.Test

class PixelTest extends TemperatureToColor {
  @Test def `Visualization function should return correct values`: Unit = {

    val arrPixel = Array(Pixel(255, 255, 0, 255), Pixel(255, 0, 0, 255))

    Image(2, 1, arrPixel).output(new java.io.File("some-image-test.png"))

  }
}
