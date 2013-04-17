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

	val roger = Wire[String]("#{coolBean.name}")
	val appName = Wire[String]("${app.name}")
	
	def makeSomeNoise() {
		println("Dep name is " + dep.name)
		println("Dep2 name is " + dep2.name)
		println("DepSeq names aare " + (depSeq map { _.name } mkString))
	}
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
		} finally {
			Wire.ignoreMissingContext = false
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
