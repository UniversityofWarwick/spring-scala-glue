package uk.ac.warwick

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
