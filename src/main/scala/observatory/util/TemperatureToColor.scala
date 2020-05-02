package observatory.util

import observatory.{Color, Temperature}

trait TemperatureToColor {

  val tToColor: Iterable[(Temperature, Color)] = Iterable.apply(
    (60.0,  Color(255, 255, 255) ), // white
    (32.0,  Color(255, 0,   0)   ), // red
    (12.0,  Color(255, 255, 0)   ), // yellow
    (0.0,   Color(0,   255, 255) ), // blue
    (-15.0, Color(0,   0,   255) ), // dark blue
    (-27.0, Color(255, 0,   255) ), // violet
    (-50.0, Color(33,  0,   107) ), // purple
    (-60.0, Color(0,   0,   0)   )  // black
  )

}
