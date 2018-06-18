import akka.actor.{ActorRef, Actor, ActorSystem, Props, Terminated}

// watching athena
object Ares {
  def props(athena: ActorRef) = Props(new Ares(athena))
}
class Ares(athena: ActorRef) extends Actor {

  override def preStart() = {
    context.watch(athena)
  }

  override def postStop() = {
    println("Ares postStop....")
  }

  def receive = {
    case Terminated =>
      context.stop(self)
  }
}

// Simple actors that stops itself when a message is received
class Athena extends Actor {
  def receive = {
    case msg =>
      println(s"Athena received ${msg}")
      context.stop(self)
  }
}

object Monitoring extends App {

  val system = ActorSystem("monitoring")

  val athena = system.actorOf(Props[Athena], "athena")

  val ares = system.actorOf(Ares.props(athena), "ares")
  // I created props method in companion object just in case
//   val ares = system.actorOf(Props(classOf[Ares], athena), "ares")

  athena ! "Hi"
//  Thread.sleep(1000)
  system.terminate()

}
