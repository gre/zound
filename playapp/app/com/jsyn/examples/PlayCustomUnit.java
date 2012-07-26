package com.jsyn.examples;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;

/**
 * Play a tone using a JSyn oscillator
 * and process it using a custom unit generator.
 * @author  Phil Burk (C) 2010 Mobileer Inc
 *
 */
public class PlayCustomUnit
{
	private Synthesizer synth;
	private UnitOscillator osc;
	private CustomCubeUnit cuber;
	private LineOut lineOut;

	private void test()
	{
		synth = JSyn.createSynthesizer();
		// Add a tone generator.
		synth.add( osc = new SineOscillator() );
		// Add a tone generator.
		synth.add( cuber = new CustomCubeUnit() );
		// Add an output to the DAC.
		synth.add( lineOut = new LineOut() );
		// Connect the oscillator to the cuber.
		osc.output.connect( 0, cuber.input, 0 );
		// Connect the cuber to the right output.
		cuber.output.connect( 0, lineOut.input, 1 );
		// Send the original to the left output for comparison.
		osc.output.connect( 0, lineOut.input, 0 );
		
		osc.frequency.set( 240.0 );
		
		// Start synthesizer using default stereo output at 44100 Hz.
		synth.start();
		// We only need to start the LineOut.
		// It will pull data from the cuber and the oscillator.
		lineOut.start();
		// Sleep while the sound is generated in the background.
		try
		{
			double time = synth.getCurrentTime();
			// Sleep for a few seconds.
			synth.sleepUntil( time + 10.0 );
		} catch( InterruptedException e )
		{
			e.printStackTrace();
		}
		// Stop everything.
		synth.stop();
	}

	public static void main( String[] args )
	{
		new PlayCustomUnit().test();
	}
}
