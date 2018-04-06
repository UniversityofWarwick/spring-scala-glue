package uk.ac.warwick.spring

import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

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

class SomeServiceProviderSpringImpl extends SomeServiceProvider with SpringAppContextProvider {
  val service = new SomeService
}

class SomeServiceProviderMockImpl extends SomeServiceProvider with AppContextProvider with MockitoSugar {
  val beeper: Beeper = mock[Beeper]
  when(beeper.beepToString()).thenReturn("meep!")
  val context: DependencyResolver = mock[DependencyResolver]

  when(context.wire[Beeper]).thenReturn(beeper)
  val service = new SomeService
}

/** App code **/
trait SomeServiceProvider {
  this:AppContextProvider =>

  class SomeService {
    val beeper: Beeper = context.wire[Beeper]
    def speak: String = beeper.beepToString()
  }
}



