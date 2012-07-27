package controllers

import play.api._
import play.api.mvc._

import com.jsyn.examples._
import com.jsyn.unitgen.LineOut;

import oscillators._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import Concurrent._

import play.api.libs.json.Json._

object Application extends Controller {

  val (enum, channel) = Concurrent.broadcast[Array[Byte]]
  
  val oscillator = new SimpleOscillator(channel)

  def index = Action {
    //val stream = oscillator.getStream()
    Ok(views.html.index(""))
  }

  def stream = Action {
    Ok.stream(enum).withHeaders( ("Content-Type", "audio/wav") )
  } 

  def start = Action {
    oscillator.start()
    Ok(toJson(Map("result" -> "Started")))
  }

  def stop = Action {
    oscillator.stop()
    Ok(toJson(Map("result" -> "Stopped")))
  }
  
  def addOsc = Action {
    oscillator.addOsc()
    Ok(toJson(Map("result" -> "OK")))
  }
}
