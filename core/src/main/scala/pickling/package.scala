package scala

import scala.language.experimental.macros

import scala.reflect.{classTag,ClassTag}
import scala.tools.reflect.{ToolBox, ToolBoxError}
import scala.reflect.runtime.{universe => ru}
import ru._

package object pickling {

  implicit class PickleOps[T](picklee: T) {
    def pickle(implicit format: PickleFormat): format.PickleType = macro Compat.PickleMacros_pickle[T]
    def pickleInto(builder: PBuilder): Unit = macro Compat.PickleMacros_pickleInto[T]
    def pickleTo(output: Output[_])(implicit format: PickleFormat): Unit = macro Compat.PickleMacros_pickleTo[T]
  }

  // utilities for adding failed tests to scalatest
  implicit class objectops(obj: Any) {
    def mustBe(other: Any) = assert(obj == other, obj + " is not " + other)

    def mustEqual(other: Any) = mustBe(other)
  }

  implicit class stringops(text: String) {
    def mustContain(substring: String) = assert(text contains substring, text)

    def mustStartWith(prefix: String) = assert(text startsWith prefix, text)
  }

  implicit class listops(list: List[String]) {
    def mustStartWith(prefixes: List[String]) = {
      assert(list.length == prefixes.size, ("expected = " + prefixes.length + ", actual = " + list.length, list))
      list.zip(prefixes).foreach{ case (el, prefix) => el mustStartWith prefix }
    }
  }

  def intercept[T <: Throwable : ClassTag](body: => Any): T = {
    try {
      body
      throw new Exception(s"Exception of type ${classTag[T]} was not thrown")
    } catch {
      case t: Throwable =>
        if (classTag[T].runtimeClass != t.getClass) throw t
        else t.asInstanceOf[T]
    }
  }

  def eval(code: String, compileOptions: String = ""): Any = {
    val tb = mkToolbox(compileOptions)
    tb.eval(tb.parse(code))
  }

  def mkToolbox(compileOptions: String = ""): ToolBox[_ <: scala.reflect.api.Universe] = {
    val m = scala.reflect.runtime.currentMirror
    import scala.tools.reflect.ToolBox
    m.mkToolBox(options = compileOptions)
  }

  def scalaBinaryVersion: String = {
    val PreReleasePattern = """.*-(M|RC).*""".r
    val Pattern = """(\d+\.\d+)\..*""".r
    val SnapshotPattern = """(\d+\.\d+\.\d+)-\d+-\d+-.*""".r
    scala.util.Properties.versionNumberString match {
      case s @ PreReleasePattern(_) => s
      case SnapshotPattern(v) => v + "-SNAPSHOT"
      case Pattern(v) => v
      case _          => ""
    }
  }

  def toolboxClasspath = {
    val f = new java.io.File(s"core/target/scala-${scalaBinaryVersion}/classes")
    if (!f.exists) sys.error(s"output directory ${f.getAbsolutePath} does not exist.")
    f.getAbsolutePath
  }

  def expectError(errorSnippet: String, compileOptions: String = "",
                  baseCompileOptions: String = s"-cp ${toolboxClasspath}")(code: String) {
    intercept[ToolBoxError] {
      eval(code, compileOptions + " " + baseCompileOptions)
    }.getMessage mustContain errorSnippet
  }
}
