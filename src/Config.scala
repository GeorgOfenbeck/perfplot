package roofline

object Config {
  val isWin = false;

  val gnuplot = if (isWin) "C:\\Program Files (x86)\\gnuplot\\bin\\gnuplot.exe" else "gnuplot"

}
