package controllers

import java.io._

import play.api._
import play.api.mvc._
import play.api.http.HeaderNames._
import play.api.Play.current
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import Concurrent._
import play.api.libs.json._
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

  val chunkedAudioStream = rawStream &> chunker &> audioEncoder
  val (sharedChunkedAudioStream, _) = Concurrent.broadcast(chunkedAudioStream)

  def stream = Action {
    Ok.stream(audioHeader >>> sharedChunkedAudioStream &> Concurrent.dropInputIfNotReady(50)).
       withHeaders( (CONTENT_TYPE, audio.contentType),
                    (CACHE_CONTROL, "no-cache") )
  }


  // UI controls

  def index = Action { implicit request =>
    Ok(views.html.index(zound.getChannels))
  }

  val (controlsStream, controlsChannel) = Concurrent.broadcast[JsValue]

  def controls = WebSocket.using[JsValue] { request =>
    
    // in: handle messages from the user
    val in = 
      Enumeratee.collect[JsValue]{ case o:JsObject => o } &>>
      Iteratee.foreach { o =>
        zound.action(o)
        controlsChannel push o
      }

    (in, controlsStream)
  }

}
