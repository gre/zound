package controllers

import play.api._
import play.api.mvc._

import com.jsyn.examples._

import oscillators._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import Concurrent._

object Application extends Controller {


  val (enum, channel) = Concurrent.broadcast[Array[Byte]]

  val oscillator = new SimpleOscillator(channel).start()

  def index = Action {
    val stream = oscillator.getStream()
    Ok(views.html.index("Your new application is ready."))
  }

  def stream = Action {
    Ok.stream(enum).withHeaders( ("Content-Type", "audio/mpeg") )
  }  
  
}
