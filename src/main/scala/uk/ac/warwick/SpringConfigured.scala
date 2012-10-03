package uk.ac.warwick

import org.springframework.beans.factory.wiring.BeanConfigurerSupport
import org.springframework.beans.factory._
import org.springframework.beans.factory.annotation._

/** Trait which mirrors the @Configurable attribute found in Spring.
  * After construction the object will be subject to wiring from the
  * Spring context if one is found.
  *
  * Note the current limitation - you have to remember to call enableConfiguration
  * (or any other constructor line, but it's easier to just add this each time).
  */
trait SpringConfigured extends DelayedInit {

	/*
		Bit stupid this... delayedInit will only be called if the
		concrete class has any lines in the constructor. You could stick in any
		old code like a println() but let's define this dummy method so you
		can put it at the top of the class.

		Hopefully there's a less silly way of ensuring delayedInit runs.
		Probably the best solution is to wait until we're using Scala 2.10 and
		then reimplement this whole thing as some kind of macro (just as well
		as DelayedInit is likely being deprecated in favour of macros)
	*/
	def enableConfiguration {}

	/*
		We rely on DelayedInit introduced in Scala 2.8 to run code after
		the object's main construction has finished.
	*/
	override def delayedInit(x: => Unit) {
		x; SpringConfigurer.beanConfigurer.configureBean(this)
	}
}

class SpringConfigurer extends BeanFactoryAware with InitializingBean with DisposableBean {
	import SpringConfigurer._

	def setBeanFactory(factory: BeanFactory) {
		beanConfigurer.setBeanFactory(factory)
		//beanConfigurer.setBeanWiringInfoResolver(new AnnotationBeanWiringInfoResolver())
	}
	def afterPropertiesSet() {
		beanConfigurer.afterPropertiesSet()
	}
	def destroy() {
		beanConfigurer.destroy()
	}
}

object SpringConfigurer {
	var beanConfigurer = new BeanConfigurerSupport
}
