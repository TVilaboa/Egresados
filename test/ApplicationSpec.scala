import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test.Helpers._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/boum")).get.value must equalTo(404)
    }

    "check the service is alive" in new WithApplication {
      val login = route(FakeRequest(GET, "/api/infobae")).get

      status(login) must equalTo(OK)
      
    }
    
    "send 401 on a unauthorized request" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(401)
     
    }
  }
}
