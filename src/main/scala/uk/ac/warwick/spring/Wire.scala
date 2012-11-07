package uk.ac.warwick.spring

import org.springframework.beans.factory.config.BeanExpressionContext
import org.springframework.beans.factory.config.TypedStringValue
import org.springframework.context.expression._
import org.springframework.beans.factory.support._
import org.springframework.context._
import collection.JavaConverters._

/** Provides methods for wiring in resources from a Spring application context.
  *
  * Requires SpringConfigurer to be defined in the application context, otherwise it won't
  * be able to find the context.
  */
object Wire {

	val resolver = new StandardBeanExpressionResolver
	// private def resolver(beanFactory: AbstractBeanFactory) = 
	// 	new BeanDefinitionValueResolver(beanFactory, null, null, beanFactory.getTypeConverter)

	/** Set this to true if you want wiring methods to return null when no application context is found.
	  * Set to false to throw an exception if it is not found.
	  */
	var ignoreMissingContext = true

	/** Returns a bean of this type. Throws an exception if there are 0 matching beans
	  * or more than one matching bean. In the latter case you may want to use the alternative
	  * method that takes a bean name.
	  *
	  * A missing app context is handled as per #getContext.
	  */
	def auto[T >: Null](implicit manifest: Manifest[T]): T = {
		val clazz: Class[T] = manifest.erasure.asInstanceOf[Class[T]]
		getContext match {
			case Some(ctx) => {
				val beans = for {
					(name, bean) <- ctx.getBeansOfType(clazz).asScala
					if ctx.getBeanFactory.getBeanDefinition(name).isAutowireCandidate
				} yield bean
				if (beans.isEmpty) throw new IllegalArgumentException("No bean of %s".format(clazz))
				else if (beans.size > 1) throw new IllegalArgumentException("Ambiguous search for %s - there were %d matching beans".format(clazz, beans.size))
				else beans.head
			}
			case None => null
		}
	}

	/** Convenient shorthand that will guess whether you are specifying a bean name
	  * or a SpEL expression.
	  */
	def apply[T >: Null](string: String)(implicit manifest: Manifest[T]): T = {
		if (string.startsWith("#{"))
			value[T](string)
		else if (string.startsWith("${"))
			property(string).asInstanceOf[T] // silly cast
		else 
			named[T](string)
	}

	def property(expression: String): String = {
		getBeanFactory match {
			case Some(factory) => factory.resolveEmbeddedValue(expression)
			case None => null
		}
	}

	def value[T >: Null](expression: String)(implicit manifest: Manifest[T]): T = {
		val clazz = manifest.erasure.asInstanceOf[Class[T]]
		getBeanFactory match {
			case Some(factory) => resolver.evaluate(expression, new BeanExpressionContext(factory, null)) match {
				case bean if clazz.isInstance(bean) => bean.asInstanceOf[T]
				case bean => throw new IllegalArgumentException("Bean expression '%s' resolves to object of type %s, expected %s".format(expression, bean.getClass, clazz))
			}
			case None => null
		}
	}

	/** Returns a bean by this name. Throws an exception if the bean doesn't exist or wasn't
	  * of the right type.
	  * 
	  * A missing app context is handled as per #getContext.
	  */
	def named[T >: Null](name: String)(implicit manifest: Manifest[T]) : T = {
		val clazz = manifest.erasure.asInstanceOf[Class[T]]
		getContext match {
			case Some(ctx) => ctx.getBean(name) match {
					case bean: Any if clazz.isInstance(bean) => bean.asInstanceOf[T]
					case bean => throw new IllegalArgumentException("Bean %s is of type %s, expected %s".format(name, bean.getClass, clazz))
				}
			case None => null
		} 
	}

	private def getBeanFactory: Option[AbstractBeanFactory] = 
		getContext.map( _.getBeanFactory.asInstanceOf[AbstractBeanFactory] )

	/** All access to the context is wrapped in this function so that we can decide what to do when
	  * no context exists (such as in a unit test). We either allow it and return null, or we throw
	  * an exception.
	  */
	private def getContext: Option[ConfigurableApplicationContext] = SpringConfigurer.applicationContext match {
		case ctx: ConfigurableApplicationContext => Some(ctx)
		case null if ignoreMissingContext => None
		case _ => throw new IllegalStateException("ApplicationContext not provided by SpringConfigurer and ignoreMissingContext is false")
	}

}
