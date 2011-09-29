package org.jdave.sbt

import org.scalatools.testing._

class JDaveFramework extends Framework {
  val name = "JDave"

  def tests = Array(new JDaveAnnotationFingerPrint)
  def testRunner(p1: ClassLoader, p2: Array[Logger]) = new JDaveRunner(p1, p2)
}

class JDaveAnnotationFingerPrint extends AnnotatedFingerprint {
  def isModule = false
  def annotationName = "org.junit.runner.RunWith"
}

class JDaveRunner(testLoader: ClassLoader, loggers: Array[Logger]) extends Runner2 {
  import java.lang.reflect.Method
  import _root_.jdave.{ Specification, ExpectationFailedException }
  import _root_.jdave.runner.{ ISpecVisitor, Context, Behavior, SpecRunner, IBehaviorResults }
  val logger = loggers(0)
  
  def run(testClassName: String, fingerprint: Fingerprint, eventHandler: EventHandler, args: Array[String]) {
    runTest(testClassName, eventHandler)
  }

  def runTest(testClassName: String, eventHandler: EventHandler): Unit = {
    val testClass = Class.forName(testClassName, true, testLoader).asSubclass(classOf[Specification[_]])
    val specVisitor = new JDaveSpecVisitor(testClass.getName, eventHandler)
    new SpecRunner().run(testClass, specVisitor)
  }

  private def log(name : String, desc : String, result : Result) = {
    result match {
       case Result.Success => logger.info("%s passed.\n".format(name))
       case Result.Failure => logger.info("%s failed:\n %s".format(name, desc))
       case Result.Error => logger.info("%s exception occurred\n %s".format(name, desc))
    }
  }
  
  private def asEvent(nr: (String, String, Result, Option[Throwable])) = nr match {
    case (testname: String, desc:String, r: Result, t : Option[Throwable]) => new Event {
      log(testname, desc, r)
      val testName = testname        
      val description = desc
      val result = r
      val error = (r, t) match {
        case (Result.Error, Some(x)) =>  x
        case _ => null
      }
    }
  }

  private class JDaveSpecVisitor(testClass:String, eventHandler: EventHandler) extends ISpecVisitor {
    private def getFullName (method :String) = { "%s.%s".format(testClass, method) }
    
    def onContext(context: Context) = {}

    
    def onBehavior(behavior: Behavior) {
      behavior.run(new IBehaviorResults {
        def expected(method: Method) = { 
          eventHandler.handle(asEvent(getFullName(method.getName), "", Result.Success, None))
        }

        def unexpected(method: Method, e: ExpectationFailedException) {
          eventHandler.handle(asEvent(getFullName(method.getName), e.getMessage, Result.Failure, Some(e)))
        }

        def error(method: Method, t: Throwable) {
          eventHandler.handle(asEvent(getFullName(method.getName), t.toString, Result.Error, Some(t)))
        }
      })
    }

    def afterContext(context: Context) {}
  }
}