//import MusicController._
import MusicController.{Play, Stop}
import MusicPlayer.{StartMusic, StopMusic}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.concurrent.duration._

// Music controller messages
object MusicController {
  sealed trait ControllerMsg
  case object Play extends ControllerMsg
  case object Stop extends ControllerMsg

  def props = Props[MusicController]
}

// Music Controller
class MusicController extends Actor {

  def receive = {
    case Play =>
      println("Music started....")
    case Stop =>
      println("Music stopped....")
  }
}

// Music Player actor
object MusicPlayer {
  sealed trait PlayMsg
  case object StopMusic extends PlayMsg
  case object StartMusic extends PlayMsg

  def props = Props[MusicPlayer]
}

class MusicPlayer extends Actor {
  def receive = {
    case StopMusic =>
      println("I don't want to stop music")
    case StartMusic =>
      val controller = context.actorOf(MusicController.props, "controller")
      controller ! Play
    case _ =>
      println("Unknown message")
  }
}

object ActorCreation extends App {
  // create the 'creation' actor system
  val system = ActorSystem("creation")

  // create music player actor
  val player = system.actorOf(MusicPlayer.props, "player")

  // send startMusic message to actor
  player ! StartMusic

  system.terminate()

}
