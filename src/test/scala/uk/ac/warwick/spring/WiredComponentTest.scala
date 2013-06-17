import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers._
import org.springframework.context.support.ClassPathXmlApplicationContext
import uk.ac.warwick.spring.Beeper

class WiredComponentTest extends FunSuite {

  test("It should be possible to mock dependencies") {new SomeServiceProviderMockImpl {
    service.speak should be("meep!")
  }}

  test("It should be possible to wire dependencies from Spring") {
    val appCtx = new ClassPathXmlApplicationContext("classpath:/test.xml")


    val service = new SomeServiceProviderSpringImpl().service
    service.speak should be("beep!")
      appCtx.destroy()
  }

}

class SomeServiceProviderSpringImpl extends SomeServiceProvider with SpringAppContextProvider  {
  val service = new SomeService
}

class SomeServiceProviderMockImpl extends SomeServiceProvider with AppContextProvider with MockitoSugar {
  import org.mockito.Mockito.when

  val beeper = mock[Beeper]
  when(beeper.beepToString()).thenReturn("meep!")
  val context = mock[DependencyResolver]

  when(context.wire[Beeper]).thenReturn(beeper)
  val service = new SomeService
}

/** App code **/
trait SomeServiceProvider {
  this:AppContextProvider =>

  class SomeService {
    val beeper = context.wire[Beeper]
    def speak = beeper.beepToString()
  }
}



