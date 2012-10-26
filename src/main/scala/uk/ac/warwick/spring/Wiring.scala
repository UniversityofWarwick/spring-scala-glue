package uk.ac.warwick.spring

import org.springframework.context._
import collection.JavaConverters._

/** Provides methods for wiring in resources from a Spring application context.
  *
  * Requires SpringConfigurer to be defined in the application context, otherwise it won't
  * be able to find the context.
  */
object Wiring {

	/** Returns a bean of this type. Throws an exception if there are 0 matching beans
	  * or more than one matching bean. In the latter case you may want to use the alternative
	  * method that takes a bean name.
	  *
	  * A missing app context is handled as per #getContext.
	  */
	def autowired[T >: Null](implicit manifest: Manifest[T]): T = {
		val clazz: Class[T] = manifest.erasure.asInstanceOf[Class[T]]
		getContext match {
			case Some(ctx) => {
				val map = ctx.getBeansOfType(clazz)
				if (map.isEmpty) throw new IllegalArgumentException("No bean of %s".format(clazz))
				else if (map.size > 1) throw new IllegalArgumentException("Ambiguous search for %s - there were %d matching beans".format(clazz, map.size))
				else map.values.iterator.next
			}
			case None => null
		}
	}

	/** Returns a bean by this name. Throws an exception if the bean doesn't exist or wasn't
	  * of the right type.
	  * 
	  * A missing app context is handled as per #getContext.
	  */
	def wired[T >: Null](name: String)(implicit manifest: Manifest[T]) : T = {
		val clazz = manifest.erasure.asInstanceOf[Class[T]]
		getContext match {
			case Some(ctx) => ctx.getBean(name) match {
					case bean: Any if clazz.isInstance(bean) => bean.asInstanceOf[T]
					case bean => throw new IllegalArgumentException("Bean %s is of type %s, expected %s".format(name, bean.getClass, clazz))
				}
			case None => null
		} 
	}

	/** Set this to true if you want wiring methods to return null when no application context is found.
	  * Useful for unit tests that don't use an application context but want to create an object that uses wiring.
	  */
	var ignoreMissingContext = false

	/** All access to the context is wrapped in this function so that we can decide what to do when
	  * no context exists (such as in a unit test). We either allow it and return null, or we throw
	  * an exception.
	  */
	private def getContext: Option[ApplicationContext] = SpringConfigurer.applicationContext match {
		case ctx: ApplicationContext => Some(ctx)
		case null if ignoreMissingContext => None
		case _ => throw new IllegalStateException("ApplicationContext not provided by SpringConfigurer and ignoreMissingContext is false")
	}

}
