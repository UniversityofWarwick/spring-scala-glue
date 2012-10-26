package uk.ac.warwick.spring

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers._

import org.springframework.context.support._

import org.springframework.context._
import org.springframework.beans.factory.wiring.BeanConfigurerSupport
import org.springframework.beans.factory._
import org.springframework.beans.factory.annotation._

class MyDependency(val name: String)

class NamedDependency(val name: String)

class WiredCommand {
	import uk.ac.warwick.spring.Wiring._

	val dep = autowired[MyDependency]
	val dep2 = wired[NamedDependency]("coolBean")

	def makeSomeNoise() {
		println("Dep name is " + dep.name)
		println("Dep2 name is " + dep2.name)
	}
}

package components {
	class WiredCommand2 extends SpringConfigured {
		import uk.ac.warwick.spring.Wiring._

		val dep = autowired[MyDependency]
	}
}

class ResourceTest extends FunSuite {

	test("Command should be autowired from bean") {
		useCtx("classpath:/test.xml") { ctx =>
	  		val cmd = new WiredCommand()
			cmd.makeSomeNoise()
		}
	}

	test("Command is missing dependencies") {
		useCtx("classpath:/test2.xml") { ctx =>
  			evaluating {  new WiredCommand() } should produce [IllegalArgumentException]
  		}
	}

	test("Missing context") {
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
			Wiring.ignoreMissingContext = true
			val cmd = new WiredCommand()
			cmd.dep should be (null)
			cmd.dep2 should be (null)
		} finally {
			Wiring.ignoreMissingContext = false
		}
	}


	/** Use the given ApplicationContext, ensuring it's shutdown afterwards. */
	def useCtx[T <: ConfigurableApplicationContext](ctx: T)(body: (T)=>Unit) {
		try body(ctx)
		finally ctx.close()
	}

	/** Use the given ClassPathXmlApplicationContext, ensuring it's closed after. */
	def useCtx(path: String)(body: (ClassPathXmlApplicationContext)=>Unit) : Unit = useCtx(new ClassPathXmlApplicationContext(path))(body)

}
