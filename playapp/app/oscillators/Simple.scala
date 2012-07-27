package oscillators

import com.jsyn._
import com.jsyn.io._
import com.jsyn.unitgen._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import Concurrent._

class SimpleOscillator(output: Channel[Array[Byte]])  {

  //val lineOut = new LineOut()
  val out = new MonoStreamWriter()

  val synth = {
    val synth = JSyn.createSynthesizer()
    val osc = new SineOscillator()
    val lag = new LinearRamp() 
		
		// Add a tone generator. (band limited sawtooth)
		synth.add(osc)
		// Add a lag to smooth out amplitude changes and avoid pops.
		synth.add(lag)

		// Add an output mixer.
		//synth.add(lineOut);
    
    synth.add(out)
    out.start()

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
    osc.output.connect(out)
    /*
		// Connect the oscillator to both left and right output.
		osc.output.connect( 0, lineOut.input, 0 )
		osc.output.connect( 0, lineOut.input, 1 )
    */
		
		// Set the minimum, current and maximum values for the port.
		lag.output.connect( osc.amplitude )
		lag.input.setup( 0.0, 0.5, 1.0 )
		lag.time.set(  0.2 )

		osc.frequency.setup( 50.0, 300.0, 10000.0 )
    synth
  }

  def start() = {
		synth.start()
		//lineOut.start()
    this
  }

  def stop() = {
    synth.stop()
    this
  }

  def getStream() = {

  }
}
