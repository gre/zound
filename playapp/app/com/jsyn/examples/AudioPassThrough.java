package com.jsyn.examples;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.LineIn;
import com.jsyn.unitgen.LineOut;

/**
 * Pass audio input to audio output.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 * 
 */
public class AudioPassThrough
{
	Synthesizer synth;
	LineIn lineIn;
	LineOut lineOut;

	private void test()
	{
		// Create a context for the synthesizer.
		synth = JSyn.createSynthesizer();
		// Add an audio input.
		synth.add( lineIn = new LineIn() );
		// Add an audio output.
		synth.add( lineOut = new LineOut() );
		// Connect the input to the output.
		lineIn.output.connect( 0, lineOut.input, 0 );
		lineIn.output.connect( 1, lineOut.input, 1 );

		// Both stereo.
		int numInputChannels = 2;
		int numOutputChannels = 2;
		synth.start( 44100, AudioDeviceManager.USE_DEFAULT_DEVICE, numInputChannels, AudioDeviceManager.USE_DEFAULT_DEVICE,
				numOutputChannels );

		// We only need to start the LineOut. It will pull data from the LineIn.
		lineOut.start();
		// Sleep a while.
		try
		{
			double time = synth.getCurrentTime();
			// Sleep for a few seconds.
			synth.sleepUntil( time + 4.0 );
		} catch( InterruptedException e )
		{
			e.printStackTrace();
		}
		// Stop everything.
		synth.stop();
	}

	public static void main( String[] args )
	{
		new AudioPassThrough().test();
	}
}
