package generators

import com.jsyn._
import com.jsyn.io._
import com.jsyn.unitgen._
import play.api._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import Concurrent._
import play.api.libs.json._
import play.api.libs.json.Json._

import scala.util.Random

// Create 4 or 5 oscillators
// - can be On/Off
// - can switch between a few frequencies (to keep it "musical")
// - can change wave types
// Advanced:
// - with LFO per each (changes freq)
// - fitler over all oscs.


/*
case class Oscillator ( enable: Boolean, wave: String, freq: Double )

object Oscillator {

  def fromUnitOscillator (osc: UnitOscillator) : Oscillator = {
  }
}
*/

class ZoundGenerator(output: Channel[Array[Double]]) {

  val out = new MonoStreamWriter()
  val hz = Array(261.6, 329.6, 392.0)
  val mul = Array(0.25, 0.5, 1, 2, 3)
  val rand = new Random(System.currentTimeMillis());

  val synth = {
    val synth = JSyn.createSynthesizer()
    synth.add(out)
    out.setOutputStream(new AudioOutputStream(){
      def close() {}
      def write(value: Double) {
        output.push(Array(value))
      }
      def write(buffer: Array[Double]) {
        write(buffer, 0, buffer.length)
      }
      def write(buffer: Array[Double], start: Int, count: Int) {
        output.push(buffer.slice(start, start+count))
      }
    })
    synth
  }

  def action (o: JsObject) {
    (o\"type", o\"osc", o\"value") match {

      case (JsString("osc-freq"), osc: JsNumber, freq: JsNumber) =>
        oscFreq(osc.value.toInt, freq.value.toDouble)

      case (JsString("osc-volume"), osc: JsNumber, volume: JsNumber) =>
        oscVolume(osc.value.toInt, volume.value.toDouble)

      case (JsString("osc-wave"), osc: JsNumber, wave: JsString) =>
        oscWave(osc.value.toInt, wave.value)

      case _ =>
    }
  }

  
  import scala.concurrent.stm._;
  val oscList = List[Ref[UnitOscillator]](Ref(addRandomOsc()), Ref(addRandomOsc()), Ref(addRandomOsc()))
  
  def addRandomOsc() = {
    addOsc("sine", 0.2, hz(rand.nextInt(hz.length)) * mul(rand.nextInt(mul.length)))
  }

  def addOsc(waveType: String, amp: Double, freq: Double) = {
    val osc = waveType match {
      case "sine" => new SineOscillator()
      case "saw" => new SawtoothOscillator()
      case "square" => new SquareOscillator()
      case "pulse" => new PulseOscillator()
      case "noise" => new RedNoise()
      case _ => new SineOscillator()
    }
    osc.frequency.set(freq)
    osc.amplitude.set(amp)
    synth.add(osc)
    osc.output.connect(out)
    osc.stop()
    osc
  }

  def removeOsc(osc: UnitOscillator) = {
    osc.output.disconnect(out.getInput)
    osc.stop()
    synth.remove(osc)
  }

  def oscVolume(oscIndex:Int, volume: Double) {
    oscList(oscIndex).single().amplitude.set(volume)
  }

  def start() = {
    synth.start()
    out.start()
    this
  }

  def stop() = {
    synth.stop()
    out.stop()
    this
  }

  def oscFreq(oscIndex:Int, freq:Double) = {
    oscList(oscIndex).single().frequency.set(freq)
  }

  def oscWave(oscIndex:Int, waveType:String) = {
    val oldOsc = oscList(oscIndex).single()
    val freq = oldOsc.frequency.get(0)
    val amp = oldOsc.amplitude.get(0)
    removeOsc(oldOsc)
    val osc = addOsc(waveType, amp, freq)
    oscList(oscIndex).single() = osc

    // val freq = oscList(oscIndex).single().frequency.get()
    // val amp = oscList(oscIndex).single().amplitude.get()
    // if(waveType == "sine") {
    //   println("ammm sine")
    //   oscList(oscIndex).single() = new SineOscillator()
    // }
    // if(waveType == "saw") {
    //   println("ammm saw")
    //   synth.remove(oscList(oscIndex).single())

    //   //synth.add(new PinkNoise())
    // }
    // val osc = oscList(oscIndex).single()
    // osc.frequency.set(freq)
    // osc.amplitude.set(amp)
    // osc
    // println(waveType, freq, amp)
  }
}
