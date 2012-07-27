package oscillators

import com.jsyn._
import com.jsyn.io._
import com.jsyn.unitgen._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import Concurrent._

class SimpleOscillator(output: Channel[Array[Byte]])  {

  val lineOut = new LineOut()
  val out = new MonoStreamWriter()

  val synth = {
    val synth = JSyn.createSynthesizer()
    synth.add(lineOut)
    synth.add(out)
    out.setOutputStream(new AudioOutputStream(){
      def close() {
        println("CLOSE!")
      }
      def write(value: Double) {
        println("write(value)")
      }
      def write(buffer: Array[Double]) {
        println("write(buffer)")
      }
      def write(buffer: Array[Double], start: Int, count: Int) {
        val buf = java.nio.ByteBuffer.allocate(8*buffer.length)
        buf.asDoubleBuffer.put(java.nio.DoubleBuffer.wrap(buffer))
        output.push(buf.array)
      }
    })
    synth
  }

  addOsc()

  def addOsc() = {
    val osc = new SineOscillator()
    osc.frequency.setup(10.0, new scala.util.Random().nextInt(1000), 15000.0)
    synth.add(osc)
    osc.output.connect( 0, lineOut.input, 0 )
    osc.output.connect( 0, lineOut.input, 1 )
    this
  }

  def start() = {
    synth.start()
    out.start()
    lineOut.start()
    this
  }

  def stop() = {
    synth.stop()
    out.stop()
    lineOut.stop()
    this
  }

  def getStream() = {

  }
}
