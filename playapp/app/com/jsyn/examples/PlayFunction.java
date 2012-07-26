package com.jsyn.examples;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.Function;
import com.jsyn.unitgen.FunctionOscillator;
import com.jsyn.unitgen.LineOut;

/**
 * Play a tone using a FunctionOscillator.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 * 
 */
public class PlayFunction
{
	Synthesizer synth;
	FunctionOscillator osc;
	LineOut lineOut;

	private void test()
	{
		// Create a context for the synthesizer.
		synth = JSyn.createSynthesizer();
		
		// Start synthesizer using default stereo output at 44100 Hz.
		synth.start();

		// Add a FunctionOscillator
		synth.add( osc = new FunctionOscillator() );
		
		// Define a function that gives the shape of the waveform.
		Function func = new Function(){
			public double evaluate( double input )
			{
				// Input ranges from -1.0 to 1.0
				double s = Math.sin( input * Math.PI * 2.0 );
				double cubed = s * s * s;
				return cubed;
			}};
		osc.function.set( func );
		
		// Add a stereo audio output unit.
		synth.add( lineOut = new LineOut() );

		// Connect the oscillator to both channels of the output.
		osc.output.connect( 0, lineOut.input, 0 );
		osc.output.connect( 0, lineOut.input, 1 );

		// Set the frequency and amplitude for the sine wave.
		osc.frequency.set( 345.0 );
		osc.amplitude.set( 0.6 );

		// We only need to start the LineOut. It will pull data from the
		// oscillator.
		lineOut.start();

		System.out.println( "You should now be hearing a sine wave. ---------" );

		// Sleep while the sound is generated in the background.
		try
		{
			double time = synth.getCurrentTime();
			// Sleep for a few seconds.
			synth.sleepUntil( time + 4.0 );
		} catch( InterruptedException e )
		{
			e.printStackTrace();
		}

		System.out.println( "Stop playing. -------------------" );
		// Stop everything.
		synth.stop();
	}

	public static void main( String[] args )
	{
		new PlayFunction().test();
	}
}
