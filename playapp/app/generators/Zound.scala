package generators

import com.jsyn._
import com.jsyn.io._
import com.jsyn.unitgen._
import play.api._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import Concurrent._

import scala.util.Random

// Create 4 or 5 oscillators
// - can be On/Off
// - can switch between a few frequencies (to keep it "musical")
// - can change wave types
// Advanced:
// - with LFO per each (changes freq)
// - fitler over all oscs.

class Zound(output: Channel[Array[Double]]) {

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
  
  import scala.concurrent.stm._;
  val oscList = List[Ref[UnitOscillator]](Ref(addOsc()), Ref(addOsc()), Ref(addOsc()))
  
  def addOsc() = {
    val osc = new SineOscillator()
    val freq = hz(rand.nextInt(hz.length)) * mul(rand.nextInt(mul.length))
    //osc.frequency.setup(0.3, freq, 15000.0)
    osc.frequency.set(freq)
    osc.amplitude.set(6.0)
    // val lfo = new SineOscillator()
    // lfo.frequency.set( 0.1 )
    // lfo.amplitude.set(freq)
    // synth.add(lfo)
    // lfo.output.connect(osc.frequency)
    synth.add(osc)
    osc.output.connect(out)
    osc.stop()
    osc
  }

  def oscOn(oscIndex:Int) = {
    println(oscIndex, "on")
    oscList(oscIndex).single().amplitude.set(6.0)
    oscIndex
  }

  def oscOff(oscIndex:Int) = {
    println(oscIndex, "off")
    oscList(oscIndex).single().amplitude.set(0.0)
    oscIndex
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
    println("osc:", oscIndex, freq, "freq")
    oscList(oscIndex).single().frequency.set(freq)
  }

  def oscWave(oscIndex:Int, waveType:String) = {

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
