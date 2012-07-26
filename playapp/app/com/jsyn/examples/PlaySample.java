package com.jsyn.examples;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.VariableRateDataReader;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.jsyn.unitgen.VariableRateStereoReader;
import com.jsyn.util.SampleLoader;

/**
 * Play a sample from a WAV file using JSyn.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 * 
 */
public class PlaySample
{
	private Synthesizer synth;
	private VariableRateDataReader samplePlayer;
	private LineOut lineOut;

	//File sampleFile = new File("samples/aaClarinet.wav");
	//File sampleFile = new File("samples/sine_400_mono.wav");
	File sampleFile = new File("samples/sine_200_300_stereo.wav");

	private void test()
	{
		synth = JSyn.createSynthesizer();

		FloatSample sample;
		try
		{
			// Add an output mixer.
			synth.add( lineOut = new LineOut() );
			// Start synthesizer using default stereo output at 44100 Hz.
			synth.start();

			// Load the sample and queue it to the player!
			sample = SampleLoader.loadFloatSample( sampleFile );
			System.out.println( "Sample has: " + sample.getChannelsPerFrame()
					+ " channels" );
			System.out.println( "            " + sample.getNumFrames()
					+ " frames" );
			System.out.println( "            " + sample.getFrameRate()
					+ " sample rate" );

			if( sample.getChannelsPerFrame() == 1 )
			{
				synth.add( samplePlayer = new VariableRateMonoReader() );
				samplePlayer.output.connect( 0, lineOut.input, 0 );
			}
			else if( sample.getChannelsPerFrame() == 2 )
			{
				synth.add( samplePlayer = new VariableRateStereoReader() );
				samplePlayer.output.connect( 0, lineOut.input, 0 );
				samplePlayer.output.connect( 1, lineOut.input, 1 );
			}
			else
			{
				throw new RuntimeException(
						"Can only play mono or stereo samples." );
			}

			samplePlayer.rate.set( sample.getFrameRate() );
			samplePlayer.dataQueue.queue( sample );
			// Connect the player to the output.

			// We only need to start the LineOut. It will pull data from the
			// oscillator.
			lineOut.start();

			// Sleep while the sound is generated in the background.
			try
			{
				// Wait until the sample has finished playing.
				do
				{
					synth.sleepFor( 1.0 );
				} while( samplePlayer.dataQueue.hasMore() );
			} catch( InterruptedException e )
			{
				e.printStackTrace();
			}
		} catch( IOException e1 )
		{
			e1.printStackTrace();
		} catch( UnsupportedAudioFileException e1 )
		{
			e1.printStackTrace();
		}
		// Stop everything.
		synth.stop();
	}

	public static void main( String[] args )
	{
		new PlaySample().test();
	}
}
