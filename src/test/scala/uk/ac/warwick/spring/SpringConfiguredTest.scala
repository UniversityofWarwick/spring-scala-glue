package uk.ac.warwick.spring

import org.scalatest._

import org.springframework.context.support._

import org.springframework.beans.factory.wiring.BeanConfigurerSupport
import org.springframework.beans.factory._
import org.springframework.beans.factory.annotation._

class Beeper {
	def beep() { println("beep!") }
}

class MyCommand extends SpringConfigured {
	@Autowired(required=true) var beeper: Beeper = _	

	def makeSomeNoise() {
		beeper.beep()
	}
}

class SpringConfiguredTest extends FunSuite {

	test("SpringConfigured object should obey autowiring") {
		val appCtx = new ClassPathXmlApplicationContext("classpath:/test.xml")

  		val cmd = new MyCommand()
		cmd.makeSomeNoise()

		appCtx.destroy()
	}

}
