import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import StatusCodes._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._


case class ErrorMessage(success: Boolean, message: String)
case class User(name: String)
case class UserResponse(message: String)

// Teach SprayJson about all our objects
// https://github.com/spray/spray-json
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val errorFormat = jsonFormat2(ErrorMessage)
  implicit val userObject = jsonFormat1(User)
  implicit val userResponse = jsonFormat1(UserResponse)
}

class MyService() extends Directives with JsonSupport {

  implicit val system = ActorSystem("omg-scala-template")
  implicit val executionContext = system.dispatcher

  val exceptionHandler = ExceptionHandler {
    case e: Exception =>
      extractUri { uri =>
        println(s"Request to $uri failed: " + e.toString)
        complete((InternalServerError, ErrorMessage(false, s"Internal error: ${e.getMessage}")))
      }
  }

  val rejectionHandler = RejectionHandler.newBuilder()
    .handle { case MalformedRequestContentRejection(msg, _) =>
      complete((BadRequest, ErrorMessage(false, msg)))
    }
    .handleAll[MethodRejection] { methodRejections =>
      val names = methodRejections.map(_.supported.name)
      complete((MethodNotAllowed, ErrorMessage(false, s"Invalid method! Supported: ${names mkString " or "}!")))
    }
    .handleNotFound { complete((NotFound, ErrorMessage(false, "Invalid route."))) }
    .result()

  val route =
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        extractRequestContext { ctx =>
          path("message") {
            post {
              entity(as[User]) { user =>
                val response = UserResponse(s"Hello ${user.name}")
                complete(response)
              }
            }
          }
        }
      }
    }
}

object MyApp {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("omg-scala-template")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher


    val myService = new MyService()
    val bindingFuture = Http().bindAndHandle(myService.route, "0.0.0.0", 8080)

    println(s"Server online at http://0.0.0.0:8080")
  }
}
