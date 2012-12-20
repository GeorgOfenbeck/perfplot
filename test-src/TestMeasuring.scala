/**
 * Georg Ofenbeck
 First created:
 * Date: 17/12/12
 * Time: 09:59
 */

import org.scalatest.Suite

import roofline.plot._
import roofline.services._
import services.CCompile

class TestMeasuring extends Suite{




  def test_Compile()
  {
    object cc extends CCompile


    cc.compile(null)
  }


}
