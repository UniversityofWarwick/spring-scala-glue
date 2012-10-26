package uk.ac.warwick.spring

import org.springframework.context._
import org.springframework.beans._
import org.springframework.beans.factory.wiring.BeanConfigurerSupport
import org.springframework.beans.factory._
import org.springframework.beans.factory.annotation._

/** Trait which mirrors the @Configurable attribute found in Spring.
  * After construction the object will be subject to wiring from the
  * Spring context if one is found.
  */
trait SpringConfigured {
	SpringConfigurer.beanConfigurer.configureBean(this)
}

class SpringConfigurer extends ApplicationContextAware with InitializingBean with DisposableBean {
	import SpringConfigurer._

	def setApplicationContext(ctx: ApplicationContext) {
		applicationContext = ctx
		ctx match {
			case ctx: ConfigurableApplicationContext => beanConfigurer.setBeanFactory(ctx.getBeanFactory)
			case _ => throw new BeanInstantiationException(getClass, "ConfigurableApplicationContext required")
		}
	}

	def afterPropertiesSet() {
		beanConfigurer.afterPropertiesSet()
	}

	def destroy() {
		//beanConfigurer.destroy()
		applicationContext = null
	}
}

object SpringConfigurer {
	var beanConfigurer = new BeanConfigurerSupport
	var applicationContext: ApplicationContext = null
}
