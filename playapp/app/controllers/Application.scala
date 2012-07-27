package controllers

import java.io._

import play.api._
import play.api.mvc._

import com.jsyn.examples._
import com.jsyn.unitgen.LineOut;

import oscillators._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import Concurrent._

import play.api.libs.json.Json._
import util._

object Application extends Controller {

  val (enum, channel) = Concurrent.broadcast[Array[Double]]

  val oscillator = new Zounds(channel).start()

  lazy val chunked = Concurrent.broadcast1((enum /*&> Enumeratee.grouped( 
      Traversable.take[Array[Double]](5000) &>>
        Iteratee.consume()
        )*/) &> waveEncoder)._1

  val waveHeader = Enumerator({
    val baos = new ByteArrayOutputStream()
    new HeaderWaveWriter(baos)
    baos.toByteArray
  })

  val waveEncoder = Enumeratee.map[Array[Double]] { _.flatMap { d =>
        var i = (32767. * d).toInt
        if(i > 32767)
          i = 32767;
        else
          if(i < -32768)
            i = -32768;
        val array = Array(i.toByte, (i>>8).toByte) //Array((i.toInt&0xFF).toByte, ((i.toInt>>8)&0xFF).toByte)
        array
      }
  }


  def index = Action {
    //val stream = oscillator.getStream()
    Ok(views.html.index(""))
  }

  def stream = Action {
    val e = (waveHeader) >>> chunked
    Ok.stream(e).withHeaders( ("Content-Type", "audio/wav") )
  } 

  def start = Action {
    oscillator.start()
    Ok(toJson(Map("result" -> "Started")))
  }

  def stop = Action {
    oscillator.stop()
    Ok(toJson(Map("result" -> "Stopped")))
  }

  def oscOn(osc:Int) = Action {
    oscillator.oscOn(osc);
    Ok(toJson(Map("result" -> "Osc on")))
  }
  def oscOff(osc:Int) = Action {
    oscillator.oscOff(osc);
    Ok(toJson(Map("result" -> "Osc off")))
  }
  
  def addOsc = Action {
    oscillator.addOsc()
    Ok(toJson(Map("result" -> "OK")))
  }

  def oscFreq(osc:Int, freq:Double) = Action {
    oscillator.oscFreq(osc, freq);
    Ok(toJson(Map("result" -> "Freq changed")))
  }

  def oscWave(osc:Int, wave:String) = Action {
    oscillator.oscWave(osc, wave);
    Ok(toJson(Map("result" -> "Wave changed")))
  }
}
