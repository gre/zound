package controllers

import java.io._

import play.api._
import play.api.mvc._
import play.api.http.HeaderNames._
import play.api.Play.current
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import Concurrent._
import play.api.libs.json.Json._

import generators._
import encoders._

object Application extends Controller {

  val (rawStream, channel) = Concurrent.broadcast[Array[Double]]

  val zound = new ZoundGenerator(channel).start()

  val audio = MonoWaveEncoder() // For now we are using WAVE

  val audioHeader = Enumerator(audio.header)
  val audioEncoder = Enumeratee.map[Array[Double]](audio.encodeData)
  
  val chunker = Enumeratee.grouped(Traversable.take[Array[Double]](5000) &>> Iteratee.consume())

  val chunkedAudioStream = (rawStream &> chunker &> audioEncoder)

  def index = Action {
    Ok(views.html.index())
  }

  def stream = Action {
    Ok.stream(audioHeader >>> chunkedAudioStream).
       withHeaders( (CONTENT_TYPE, audio.contentType),
                    (CACHE_CONTROL, "no-cache") )
  } 

  def oscOn(osc:Int) = Action {
    zound.oscOn(osc);
    Ok(toJson(Map("result" -> "Osc on")))
  }
  def oscOff(osc:Int) = Action {
    zound.oscOff(osc);
    Ok(toJson(Map("result" -> "Osc off")))
  }
  
  def addOsc = Action {
    zound.addOsc()
    Ok(toJson(Map("result" -> "OK")))
  }

  def oscFreq(osc:Int, freq:Double) = Action {
    zound.oscFreq(osc, freq);
    Ok(toJson(Map("result" -> "Freq changed")))
  }

  def oscWave(osc:Int, wave:String) = Action {
    zound.oscWave(osc, wave);
    Ok(toJson(Map("result" -> "Wave changed")))
  }

}
