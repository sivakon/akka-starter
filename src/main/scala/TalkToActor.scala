import Checker._
import Storage._
import Recorder._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.duration._

case class User(username: String, email: String)

object Recorder {
  sealed trait RecorderMsg

  case class NewUser(user: User) extends RecorderMsg

  def props(checker: ActorRef, storage: ActorRef) =
    Props(new Recorder(checker, storage))
}

object Checker {
  sealed trait CheckerMsg
  // Checker message
  case class CheckUser(user: User) extends CheckerMsg

  sealed trait CheckerResponse
  // Checker response messages
  case class BlackUser(user: User) extends CheckerResponse
  case class WhiteUser(user: User) extends CheckerResponse
}

object Storage {
  sealed trait StorageMsg
  // storage message
  case class AddUser(user: User) extends StorageMsg
}

class Storage extends Actor {

  var users = List.empty[User]

  def receive = {
    case AddUser(user) =>
      println(s"Storage: $user added")
      users = user :: users
  }
}

class Checker extends Actor {

  val blackList = List(
    User("Adam", "adam@gmail.com")
  )

  def receive = {
    case CheckUser(user) if blackList.contains(user) =>
      println(s"Checker: $user in the blacklist")
      sender() ! BlackUser(user)
    case CheckUser(user) =>
      println(s"Checker: $user not in the blacklist")
      sender() ! WhiteUser(user)
  }
}

class Recorder(checker: ActorRef, storage: ActorRef) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(5 seconds)

  def receive = {
    case NewUser(user) =>
      println(s"Recorder receives NewUser for ${user}")
      checker ? CheckUser(user) map {
        case WhiteUser(user) =>
          storage ! Storage.AddUser(user)
        case BlackUser(user) =>
          println(s"Recorder: ${user} in in black user.")
      }

    case _ =>
      println("Please input a user")
  }
}

object TalkToActor extends App {
  // create actor system
  val system = ActorSystem("talk-to-actor")

  val checker = system.actorOf(Props[Checker], "checker")

  val storage = system.actorOf(Props[Storage], "storage")

  val recorder = system.actorOf(Recorder.props(checker, storage), "recorder")

  recorder ! Recorder.NewUser(User("Jon", "jon@outlook.com"))

  recorder ! Recorder.NewUser(User("Siva", "siva@outlook.com"))

  recorder ! Recorder.NewUser(User("Adam", "adam@gmail.com"))

  Thread.sleep(100)

  system.terminate()
}
