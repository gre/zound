package oscillators

import com.jsyn._
import com.jsyn.io._
import com.jsyn.unitgen._
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


class SimpleOscillator(output: Channel[Array[Double]])  {

  //val lineOut = new LineOut()
  val out = new MonoStreamWriter()

  val hz = Array(261.6, 329.6, 392.0)
  val mul = Array(0.25, 0.5, 1, 2, 3)
  val rand = new Random(System.currentTimeMillis());

  val synth = {
    val synth = JSyn.createSynthesizer()
    //synth.add(lineOut)
    synth.add(out)
    out.setOutputStream(new AudioOutputStream(){
      def close() {
      }
      def write(value: Double) {
        output.push(Array(value))
      }
      def write(buffer: Array[Double]) {
        write(buffer, 0, buffer.length)
      }
      def write(buffer: Array[Double], start: Int, count: Int) {
        output.push(buffer.slice(start, start+count))
        /*
        val buf = java.nio.ByteBuffer.allocate(8*buffer.length)
        buf.asDoubleBuffer.put(java.nio.DoubleBuffer.wrap(buffer))
        output.push(buf.array)
        */
      }
    })
    synth
  }

  addOsc()

  def addOsc() = {
    val osc = new SineOscillator()

    val freq = hz(rand.nextInt(hz.length)) * mul(rand.nextInt(mul.length))

    osc.frequency.setup(10.0, freq, 15000.0)
    // val lfo = new SineOscillator()
    // lfo.frequency.set( 0.1 )
    // lfo.amplitude.set(freq)
    // synth.add(lfo)
    // lfo.output.connect(osc.frequency)

    synth.add(osc)
    osc.output.connect(out)
    //osc.output.connect( 0, lineOut.input, 0 )
    //osc.output.connect( 0, lineOut.input, 1 )
    this
  }

  def start() = {
    synth.start()
    out.start()
    //lineOut.start()
    this
  }

  def stop() = {
    synth.stop()
    out.stop()
    //lineOut.stop()
    this
  }

  def getStream() = {

  }
}
