package com.jsyn.examples;

import com.jsyn.ports.UnitInputPort;
import com.jsyn.unitgen.UnitOscillator;

public class GoogleWaveOscillator extends UnitOscillator
{
	public UnitInputPort variance;
	private double phaseIncrement = 0.1;
	private double previousY;
	private double randomAmplitude = 0.0;

	public GoogleWaveOscillator()
	{
		addPort( variance = new UnitInputPort( "Variance", 0.0 ) );
	}

	@Override
	public void generate( int start, int limit )
	{
		// Get signal arrays from ports.
		double[] freqs = frequency.getValues();
		double[] outputs = output.getValues();
		double currentPhase = phase.getValue();
		double y;

		for( int i = start; i < limit; i++ )
		{
			if( currentPhase > 0.0 )
			{
				double p = currentPhase;
				y = Math.sqrt( 4.0 * (p * (1.0 - p)) );
			}
			else
			{
				double p = -currentPhase;
				y = -Math.sqrt( 4.0 * (p * (1.0 - p)) );
			}

			if( (previousY * y) <= 0.0 )
			{
				// Calculate randomly offset phaseIncrement.
				double v = variance.getValues()[0];
				double range = ((Math.random() - 0.5) * 4.0 * v);
				double scale = Math.pow(2.0, range );
				phaseIncrement = convertFrequencyToPhaseIncrement( freqs[i] ) * scale;

				// Calculate random amplitude.
				scale = 1.0 + ((Math.random() - 0.5) * 1.5 * v);
				randomAmplitude = amplitude.getValues()[0] * scale;
			}

			outputs[i] = y * randomAmplitude;
			previousY = y;
			
			currentPhase = incrementWrapPhase( currentPhase, phaseIncrement );
		}
		phase.setValue( currentPhase );
	}
}