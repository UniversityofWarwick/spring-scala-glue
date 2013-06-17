import scala.reflect.ClassTag
import uk.ac.warwick.spring.Wire
/**
 * An alternative to Wire[T] that is suitable for use in Cake-style DI.
 *
 * Classes can use a self-type annotation of AppContextProvider, giving them access to the context.wire[T] method to resolve
 * dependencies. You can mix in the SpringWiredComponent for spring beans, and mix in a mock object for tests
 *
 */

trait AppContextProvider{
  def context: DependencyResolver

  trait DependencyResolver {
    def wire[A >: Null : ClassTag]: A
  }

}

trait SpringAppContextProvider extends AppContextProvider {
  val context = new SpringDependencyResolver
  class SpringDependencyResolver extends DependencyResolver {
    def wire[A >: Null : ClassTag] = Wire[A]
  }
}


