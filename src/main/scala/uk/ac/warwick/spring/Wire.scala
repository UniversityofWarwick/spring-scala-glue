package uk.ac.warwick.spring

import org.springframework.beans.factory.config.BeanExpressionContext
import org.springframework.beans.factory.config.TypedStringValue
import org.springframework.context.expression._
import org.springframework.beans.factory.support._
import org.springframework.beans.factory._
import org.springframework.context._
import collection.JavaConverters._
import scala.reflect._

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
	def auto[A >: Null : ClassTag]: A = {
		val clazz: Class[A] = classTag[A].runtimeClass.asInstanceOf[Class[A]]
		getContext match {
			case Some(_) => option[A] match {
				case Some(bean) => bean
				case _ => throw new IllegalArgumentException("No bean of %s".format(clazz))
			}
			case None => null
		}
	}
	def option[A >: Null : ClassTag]: Option[A] = {
		val clazz: Class[A] = classTag[A].runtimeClass.asInstanceOf[Class[A]]
		getContext flatMap { context =>
			val beans = all[A]
			if (beans.size > 1) throw new IllegalArgumentException("Ambiguous search for %s - there were %d matching beans".format(clazz, beans.size))
			else beans.headOption
		}
	}

	/**
	 * Returns all beans of type A
	 */
	def all[A >: Null : ClassTag]: Seq[A] = {
		val clazz = classTag[A].runtimeClass.asInstanceOf[Class[A]]
		getContext match {
			case Some(ctx) => {
				for {
					(name, bean) <- ctx.getBeansOfType(clazz).asScala.toSeq
					if ctx.getBeanFactory.getBeanDefinition(name).isAutowireCandidate
				} yield bean
			}
			case None => Seq()
		}
	}

	/**
	 * Convenient shorthands for Wire.auto[A]
	 */
	def apply[A >: Null : ClassTag] = required[A]
	def required[A >: Null : ClassTag] = auto[A]

	/** Convenient shorthand that will guess whether you are specifying a bean name
	  * or a SpEL expression.
	  */
	def apply[A >: Null : ClassTag](string: String): A = required[A](string)
	def required[A >: Null : ClassTag](string: String): A = 
		if (string.startsWith("#{"))
			value[A](string)
		else if (string.startsWith("${"))
			property(string).asInstanceOf[A] // silly cast
		else 
			named[A](string)

	def option[A >: Null : ClassTag](string: String): Option[A] =
		if (string.startsWith("#{"))
			optionValue[A](string)
		else if (string.startsWith("${"))
			optionProperty(string).asInstanceOf[Option[A]] // silly cast
		else 
			optionNamed[A](string)

	def property(expression: String): String = (getBeanFactory map { _.resolveEmbeddedValue(expression) }).orNull

	def optionProperty(expression: String): Option[String] =
		getBeanFactory flatMap { factory =>
			try {
				Some(factory.resolveEmbeddedValue(expression))
			} catch {
				case e: IllegalArgumentException => None
			}
		}

	def value[A >: Null : ClassTag](expression: String): A = {
		val clazz = classTag[A].runtimeClass.asInstanceOf[Class[A]]
		getBeanFactory match {
			case Some(factory) => resolver.evaluate(expression, new BeanExpressionContext(factory, null)) match {
				case bean if clazz.isInstance(bean) => bean.asInstanceOf[A]
				case bean => throw new IllegalArgumentException("Bean expression '%s' resolves to object of type %s, expected %s".format(expression, bean.getClass, clazz))
			}
			case None => null
		}
	}

	def optionValue[A >: Null : ClassTag](expression: String): Option[A] = {
		val clazz = classTag[A].runtimeClass.asInstanceOf[Class[A]]
		getBeanFactory flatMap { factory =>
			try {
				resolver.evaluate(expression, new BeanExpressionContext(factory, null)) match {
					case bean if clazz.isInstance(bean) => Some(bean.asInstanceOf[A])
					case bean => throw new IllegalArgumentException("Bean expression '%s' resolves to object of type %s, expected %s".format(expression, bean.getClass, clazz))
				}
			} catch {
				case e: BeanExpressionException => None
			}
		}
	}

	/** Returns a bean by this name. Throws an exception if the bean doesn't exist or wasn't
	  * of the right type.
	  * 
	  * A missing app context is handled as per #getContext.
	  */
  	def named[A >: Null : ClassTag](name: String): A = {
		val clazz = classTag[A].runtimeClass.asInstanceOf[Class[A]]
		getContext match {
			case Some(ctx) => ctx.getBean(name) match {
				case bean: Any if clazz.isInstance(bean) => bean.asInstanceOf[A]
				case bean => throw new IllegalArgumentException("Bean %s is of type %s, expected %s".format(name, bean.getClass, clazz))
			}
			case None => null
		} 
	}

	def optionNamed[A >: Null : ClassTag](name: String) : Option[A] = {
		val clazz = classTag[A].runtimeClass.asInstanceOf[Class[A]]
		getContext flatMap { ctx =>
			try {
				ctx.getBean(name) match {
					case bean: Any if clazz.isInstance(bean) => Some(bean.asInstanceOf[A])
					case bean => throw new IllegalArgumentException("Bean %s is of type %s, expected %s".format(name, bean.getClass, clazz))
				}
			} catch {
				case e: NoSuchBeanDefinitionException => None
			}
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
