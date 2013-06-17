package uk.ac.warwick.spring

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers._

import org.springframework.context.support._

import org.springframework.context._
import org.springframework.beans.factory.wiring.BeanConfigurerSupport
import org.springframework.beans.factory._
import org.springframework.beans.factory.annotation._

class MyDependency(val name: String)

class NamedDependency(val name: String) {
	def getName() = name
}

class WiredCommand {

	val dep = Wire.auto[MyDependency]
	val dep2 = Wire[NamedDependency]("coolBean")
	val depSeq = Wire.all[MyDependency]
	val optDep = Wire.option[MyDependency]
	val optDepMissing = Wire.option[BeanConfigurerSupport]
	val optNamedMissing = Wire.optionNamed[NamedDependency]("coolBeansFace")

	val roger = Wire[String]("#{coolBean.name}")
	val appName = Wire[String]("${app.name}")
	val appNameOpt = Wire.option[String]("${app.name}")

	val optPropMissing = Wire.option[String]("${app.blahblah}")
	val optValueMissing = Wire.optionValue[String]("#{coolBean.blahblah}")
	val optPropertyMissing = Wire.optionProperty("${app.blahblah}")

	val feature = Wire[java.lang.Boolean]("${feature.enabled:false}")
	val otherFeature = Wire[java.lang.Boolean]("${feature.another:true}")
	
	def makeSomeNoise() {
		println("Dep name is " + dep.name)
		println("Dep2 name is " + dep2.name)
		println("DepSeq names are " + (depSeq map { _.name } mkString))
		println("optDep name is " + optDep.get.name)
	}
}

class WiredCommand3 {
	val dep = Wire[NamedDependency]("coolBean")
	val dep2 = Wire[NamedDependency]("madeUpBean")
	val integers = Wire.all[java.lang.Integer]
}

class CommandWithValue {
	val appName = Wire[String]("${app.name}")
}

package components {
	class WiredCommand2 extends SpringConfigured {
		val dep = Wire[MyDependency]
	}
}

class ResourceTest extends FunSuite {

	test("Command should be autowired from bean") {
		useCtx("classpath:/test.xml") { ctx =>
	  		val cmd = new WiredCommand()
			cmd.makeSomeNoise()
			cmd.roger should be ("Roger")
			cmd.appName should be ("The best app in the world")
			cmd.appNameOpt should be (Some("The best app in the world"))
			cmd.optDep should be ('defined)
			cmd.optDepMissing should be ('empty)
			cmd.optPropMissing should be ('empty)
			cmd.optNamedMissing should be ('empty)
			cmd.optPropertyMissing should be ('empty)
			cmd.optValueMissing should be ('empty)
			cmd.feature.booleanValue() should be (true)
			cmd.otherFeature.booleanValue() should be (true)
		}
	}

	test("Placeholder values") {
		useCtx("classpath:/test.xml") { ctx =>
	  		val cmd = new CommandWithValue()
			cmd.appName should be ("The best app in the world")
		}
	}

	test("Command is missing dependencies") {
		useCtx("classpath:/test2.xml") { ctx =>
  			evaluating {  new WiredCommand() } should produce [IllegalArgumentException]
  		}
	}

	test("Missing context") {
		Wire.ignoreMissingContext = false
		evaluating { new WiredCommand() } should produce [IllegalStateException]
	}

	test("Multiple candidates") {
		evaluating {
			useCtx("classpath:/test3.xml") { ctx =>
				fail("Shouldn't have successfully created context")			
			}
		} should produce [BeanCreationException]
	}

	test("Ignore missing context") {
		try {
			Wire.ignoreMissingContext = true
			val cmd = new WiredCommand()
			cmd.dep should be (null)
			cmd.dep2 should be (null)
			cmd.depSeq should be ('empty)
			cmd.optDep should be ('empty)
			cmd.optDepMissing should be ('empty)
			cmd.optPropMissing should be ('empty)
			cmd.optNamedMissing should be ('empty)
			cmd.optPropertyMissing should be ('empty)
			cmd.optValueMissing should be ('empty)

			// Fall back to default
			cmd.feature.booleanValue() should be (false)
			cmd.otherFeature.booleanValue() should be (true)
		} finally {
			Wire.ignoreMissingContext = false
		}
	}

	test("Ignore missing beans") {
		useCtx("classpath:/test.xml") { ctx =>
			try {
				Wire.ignoreMissingBeans = true
				val bean = new WiredCommand3()
				bean.dep.name should be ("Roger")
				bean.dep2 should be (null)
				bean.integers should be (Nil)
			} finally {
				Wire.ignoreMissingBeans = false
			}
		}
	}


	/** Use the given ApplicationContext, ensuring it's shutdown afterwards. */
	def useCtx[T <: ConfigurableApplicationContext](ctx: T)(body: (T)=>Unit) {
		try body(ctx)
		finally ctx.close()
	}

	/** Use the given ClassPathXmlApplicationContext, ensuring it's closed after. */
	def useCtx(path: String)(body: (ClassPathXmlApplicationContext)=>Unit) : Unit = {
		val ctx = new ClassPathXmlApplicationContext(path)
		ctx.refresh()
		useCtx(ctx)(body)
	}

}
