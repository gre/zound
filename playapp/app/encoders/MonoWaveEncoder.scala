package encoders;

// Implementing the WAV format here!

case class MonoWaveEncoder (
  frameRate: Int = 44100,
  samplesPerFrame: Int = 1,
  bitsPerSample: Int = 16
) {
  val bytesPerSamples = ((bitsPerSample+7)/8).toInt

  val contentType = "audio/wav"

  lazy val header: Array[Byte] = {
    val riff = "RIFF".toArray.map(_.toByte) ++
               IntLittleBytes(0x7fffffff) ++ // put the maximum ChunkSize (we stream)
               "WAVE".toArray.map(_.toByte);

    val fmt =  "fmt ".toArray.map(_.toByte) ++
               IntLittleBytes(16) ++ // Subchunk1Size for PCM = 16
               ShortLittleBytes(1) ++ // AudioFormat for PCM = 1
               ShortLittleBytes(samplesPerFrame toShort) ++
               IntLittleBytes(frameRate) ++
               IntLittleBytes(frameRate*samplesPerFrame*bytesPerSamples) ++
               ShortLittleBytes(samplesPerFrame*bytesPerSamples toShort) ++
               ShortLittleBytes(bitsPerSample toShort);

    val data = "data".toArray.map(_.toByte) ++
               IntLittleBytes(0x7fffffff);

    riff ++ fmt ++ data;
  }

  def encodeData(data: Array[Double]): Array[Byte] = {
    data flatMap { d =>
      ShortLittleBytes(math.max(-1.0, math.min(d, 1.0))*0x7fff toShort)
    }
  }

  private def IntLittleBytes(i: Int) = Array(
    i     toByte,
    i>>8  toByte,
    i>>16 toByte,
    i>>24 toByte
  )

  private def ShortLittleBytes(i: Short) = Array(
    i    toByte,
    i>>8 toByte
  )

}
