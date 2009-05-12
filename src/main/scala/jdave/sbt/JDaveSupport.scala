package jdave.sbt

import _root_.sbt._

object JDaveFramework extends LazyTestFramework {
  val name = "JDave"

  def testSuperClassName = "jdave.Specification"
  def testSubClassType = ClassType.Class

  def testRunnerClassName = "jdave.sbt.JDaveRunner"
}

class JDaveRunner(val log: Logger, val listeners: Seq[TestReportListener], val testLoader: ClassLoader) extends BasicTestRunner {
import java.lang.reflect.Method
import _root_.jdave.{Specification, ExpectationFailedException}
import _root_.jdave.runner.{ISpecVisitor, Context, Behavior, SpecRunner, IBehaviorResults}

  def runTest(testClassName: String): Result.Value = {
    val testClass = Class.forName(testClassName, true, testLoader).asSubclass(classOf[Specification[_]])
    val specVisitor = new JDaveSpecVisitor
    new SpecRunner().run(testClass, specVisitor)
    specVisitor.testResult
  }

  private class JDaveSpecVisitor extends ISpecVisitor {
    var testResult = Result.Passed

    def onContext(context: Context) = log.info("  " + context.getName)

    def onBehavior(behavior: Behavior) {
      behavior.run(new IBehaviorResults {
        def expected(method: Method) = log.info("  + " + behavior.getName)

        def unexpected(method: Method, e: ExpectationFailedException) {
          log.error(" - " + behavior.getName + ": " + e.getMessage)
          log.trace(e)
          testResult = Result.Failed
        }

        def error(method: Method, t: Throwable) {
          log.error(" - " + behavior.getName + ": " + t.getMessage)
          log.trace(t)
          testResult = Result.Failed
        }
      })
    }

    def afterContext(context: Context) {}
  }
}

