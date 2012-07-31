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

case class Oscillator ( wave: String, amp: Double, freq: Double ) {
  def toUnitOscillator = Oscillator.toUnitOscillator(this)
}

object Oscillator {

  def toUnitOscillator (osc: Oscillator) : UnitOscillator = {
    val o = osc.wave match {
      case "sine" => new SineOscillator()
      case "saw" => new SawtoothOscillator()
      case "square" => new SquareOscillator()
      case "pulse" => new PulseOscillator()
      case "noise" => new RedNoise()
      case _ => new SineOscillator()
    }
    o.frequency.set(osc.freq)
    o.amplitude.set(osc.amp)
    o
  }

  def fromUnitOscillator (osc: UnitOscillator) : Oscillator = {
    Oscillator(osc match {
      case _: SineOscillator => "sine"
      case _: SawtoothOscillator => "saw"
      case _: SquareOscillator => "square"
      case _: PulseOscillator => "pulse"
      case _: RedNoise => "noise"
      case _ => "sine"
    }, osc.amplitude.get(0), osc.frequency.get(0))
  }
}


class ZoundGenerator(output: Channel[Array[Double]]) {

  val out = new MonoStreamWriter()
  val oscMixer = new DualInTwoOut()

  val hz = Array(261.6, 329.6, 392.0)
  val mul = Array(0.25, 0.5, 1, 2, 3)
  val rand = new Random(System.currentTimeMillis())

  val synth = {
    val synth = JSyn.createSynthesizer()
    synth.add(out)
    synth.add(oscMixer)

    // Add a filter
    val highPass = new FilterHighPass
    synth.add(highPass)
    oscMixer.outputA.connect(highPass.input)
    oscMixer.outputB.connect(highPass.input)
    
    highPass.frequency.set(0, 577);
    highPass.Q.set(0, 1.0);
    highPass.amplitude.set(0, 1.0);
    highPass.output.connect(out.input);

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

  def getChannels = oscList map { osc =>
    Oscillator.fromUnitOscillator(osc.single())
  }
  
  import scala.concurrent.stm._;
  val oscList = List[Ref[UnitOscillator]](Ref(addRandomOsc()), Ref(addRandomOsc()), Ref(addRandomOsc()))

  def oscN(oscIndex: Int) = oscList(oscIndex).single()
  def oscN(oscIndex: Int, newOsc: UnitOscillator) = oscList(oscIndex).single() = newOsc
  
  def addRandomOsc() = {
    connectOsc(Oscillator("sine", 0.2, hz(rand.nextInt(hz.length)) * mul(rand.nextInt(mul.length))).toUnitOscillator)
  }

  def connectOsc(osc: UnitOscillator) = {
    synth.add(osc)
    osc.output.connect(oscMixer.input)
    osc.stop()
    osc
  }

  def disconnectOsc(osc: UnitOscillator) {
    osc.output.disconnect(oscMixer.input)
    osc.stop()
    synth.remove(osc)
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

  def oscVolume(oscIndex:Int, volume: Double) {
    oscN(oscIndex).amplitude.set(volume)
  }

  def oscFreq(oscIndex:Int, freq:Double) {
    oscList(oscIndex).single().frequency.set(freq)
  }

  def oscWave(oscIndex:Int, waveType:String) {
    val oldOsc = oscN(oscIndex)
    val freq = oldOsc.frequency.get(0)
    val amp = oldOsc.amplitude.get(0)
    disconnectOsc(oldOsc)
    val osc = Oscillator(waveType, amp, freq).toUnitOscillator
    connectOsc(osc)
    oscN(oscIndex, osc)
  }
}
