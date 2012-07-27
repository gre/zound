package util;

import com.jsyn.io.AudioOutputStream;
import java.io.*;

public class HeaderWaveWriter
    implements AudioOutputStream
{

    public HeaderWaveWriter(OutputStream os)
			throws IOException
    {
        riffSizePosition = 0L;
        dataSizePosition = 0L;
        frameRate = 44100;
        samplesPerFrame = 1;
        bitsPerSample = 16;
        headerWritten = false;
				outputStream = os;
				writeHeader();
    }

    public void setFrameRate(int i)
    {
        frameRate = i;
    }

    public void setSamplesPerFrame(int i)
    {
        samplesPerFrame = i;
    }

    public void setBitsPerSample(int i)
    {
        bitsPerSample = i;
    }

    public void close()
        throws IOException
    {
        outputStream.close();
    }

    public void write(double ad[])
        throws IOException
    {
        write(ad, 0, ad.length);
    }

    public void write(double d)
        throws IOException
    {
        if(!headerWritten)
            writeHeader();
        int i = (int)(32767D * d);
        if(i > 32767)
            i = 32767;
        else
        if(i < -32768)
            i = -32768;
        writeByte(i);
        writeByte(i >> 8);
    }

    public void write(double ad[], int i, int j)
        throws IOException
    {
        for(int k = 0; k < j; k++)
            write(ad[i + k]);

    }

    private void writeByte(int i)
        throws IOException
    {
        outputStream.write(i);
        bytesWritten++;
    }

    public void writeIntLittle(int i)
        throws IOException
    {
        writeByte(i & 0xff);
        writeByte(i >> 8 & 0xff);
        writeByte(i >> 16 & 0xff);
        writeByte(i >> 24 & 0xff);
    }

    public void writeShortLittle(short word0)
        throws IOException
    {
        writeByte(word0 & 0xff);
        writeByte(word0 >> 8 & 0xff);
    }

    private void writeHeader()
        throws IOException
    {
        writeRiffHeader();
        writeFormatChunk();
        writeDataChunkHeader();
        outputStream.flush();
        headerWritten = true;
    }

    private void writeRiffHeader()
        throws IOException
    {
        writeByte(82);
        writeByte(73);
        writeByte(70);
        writeByte(70);
        riffSizePosition = bytesWritten;
        writeIntLittle(0x7fffffff);
        writeByte(87);
        writeByte(65);
        writeByte(86);
        writeByte(69);
    }

    public void writeFormatChunk()
        throws IOException
    {
        int i = (bitsPerSample + 7) / 8;
        writeByte(102);
        writeByte(109);
        writeByte(116);
        writeByte(32);
        writeIntLittle(16);
        writeShortLittle((short)1);
        writeShortLittle((short)samplesPerFrame);
        writeIntLittle(frameRate);
        writeIntLittle(frameRate * samplesPerFrame * i);
        writeShortLittle((short)(samplesPerFrame * i));
        writeShortLittle((short)bitsPerSample);
    }

    public void writeDataChunkHeader()
        throws IOException
    {
        writeByte(100);
        writeByte(97);
        writeByte(116);
        writeByte(97);
        dataSizePosition = bytesWritten;
        writeIntLittle(0x7fffffff);
    }

    private static final short WAVE_FORMAT_PCM = 1;
    private OutputStream outputStream;
    private long riffSizePosition;
    private long dataSizePosition;
    private int frameRate;
    private int samplesPerFrame;
    private int bitsPerSample;
    private int bytesWritten;
    private boolean headerWritten;
}

