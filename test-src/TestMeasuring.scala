/**
 * Georg Ofenbeck
 First created:
 * Date: 17/12/12
 * Time: 09:59
 */

import org.scalatest.Suite

import roofline.plot._
import roofline.services._
import services.{CSourceService, CompileService}
import java.io._
import java.nio.file._


class TestMeasuring extends Suite{




  def test_Compile()
  {
    val filename = "tmp"

    val test_source = "#include \"pcm/measuring_core.h\"\n#include <immintrin.h>\nint main () {\n    perfmon_init(1,false,false,false);\n    perfmon_start();\n    __m256 a, b;\n    double something = 0;\n\n    {\n      __asm{\n        movsd xmm0, xmm1\n        movsd xmm0, xmm2\n        movsd xmm0, xmm3\n        movsd xmm0, xmm4\n        movsd xmm0, xmm5\n        movsd xmm0, xmm6\n        movsd xmm0, xmm7\n        movsd xmm0, xmm8\n        movsd xmm0, xmm9\n        movsd xmm0, xmm10\n        movsd xmm0, xmm11\n        movsd xmm0, xmm12\n        movsd xmm0, xmm13\n        movsd xmm0, xmm14\n        movsd xmm0, xmm15\n      }\n    }\n\n    for (int i = 0; i< 128; i++)\n    {\n      __asm{\n        addsd     xmm0, xmm1\n        mulsd     xmm2, xmm3\n\n        addsd     xmm4, xmm5\n        mulsd     xmm6, xmm7\n\n        addsd     xmm8, xmm9\n        mulsd     xmm10, xmm11\n\n        addsd     xmm12, xmm13\n        mulsd     xmm14, xmm15\n\n\n        addpd\t  xmm0, xmm1\n        vaddpd    ymm0, ymm0, ymm1\n\n      }\n\n    }\n\n\n\n    perfmon_stop();\n    perfmon_end();\n    return 0;\n  }"
    val cmdbat = new PrintStream(filename + ".cpp")
    cmdbat.println(test_source)
    cmdbat.close()

    CompileService.compile(filename)


  }


}
