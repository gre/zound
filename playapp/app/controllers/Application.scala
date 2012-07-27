package controllers

import play.api._
import play.api.mvc._

import com.jsyn.examples._

import oscillators._

object Application extends Controller {

  val oscillator = new SimpleOscillator().start()

  def index = Action {
    val stream = oscillator.getStream()
    Ok(views.html.index("Your new application is ready."))
  }
  
}
