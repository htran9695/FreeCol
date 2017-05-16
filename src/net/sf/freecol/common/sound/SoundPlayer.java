/**
 *  Copyright (C) 2002-2015   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.sound;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import net.sf.freecol.FreeCol;
import net.sf.freecol.common.option.AudioMixerOption;
import net.sf.freecol.common.option.AudioMixerOption.MixerWrapper;
import net.sf.freecol.common.option.PercentageOption;

/**
 * Stripped down class for playing sound.
 */
public class SoundPlayer {

	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(SoundPlayer.class.getName());

	/** The mixer. */
	private Mixer mixer;

	/** The volume. */
	private int volume;

	/** The sound player thread. */
	private final SoundPlayerThread soundPlayerThread;

	/**
	 * Creates a sound player.
	 *
	 * @param mixerOption
	 *            The option for setting the mixer to use.
	 * @param volumeOption
	 *            The volume option to use when playing audio.
	 */
	public SoundPlayer(AudioMixerOption mixerOption, PercentageOption volumeOption) {
		setMixer(mixerOption.getValue());
		if (mixer == null) {
			throw new IllegalStateException("Mixer unavailable.");
		}
		mixerOption.addPropertyChangeListener((PropertyChangeEvent e) -> {
			setMixer((MixerWrapper) e.getNewValue());
		});
		setVolume(volumeOption.getValue());
		volumeOption.addPropertyChangeListener((PropertyChangeEvent e) -> {
			setVolume((Integer) e.getNewValue());
		});
		soundPlayerThread = new SoundPlayerThread();
		soundPlayerThread.start();
	}

	/**
	 * Gets an audio input stream given a file, hopefully containing audio data.
	 *
	 * @param file
	 *            The <code>File</code> to test.
	 * @return An <code>AudioInputStream</code>, or null on failure.
	 * @throws Exception
	 *             if the file does not contain valid audio.
	 */
	public static AudioInputStream getAudioInputStream(File file) throws Exception {
		AudioInputStream in;
		if (file.getName().endsWith(".ogg")) {
			// We used to use tritonus to provide ogg (strictly,
			// Vorbis-audio-in-ogg-container) decoding to the Java
			// sound system. It was buggy and appears to be
			// unmaintained since 2009. So now for ogg we have our
			// own jorbis-based decoder.
			in = new OggVorbisDecoderFactory().getOggStream(file);
		} else {
			in = AudioSystem.getAudioInputStream(file);
		}
		return in;
	}

	/**
	 * Gets the mixer.
	 *
	 * @return The current mixer.
	 */
	public Mixer getMixer() {
		return mixer;
	}

	/**
	 * Sets the mixer.
	 *
	 * @param mw
	 *            the new mixer
	 */
	private void setMixer(MixerWrapper mw) {
		try {
			mixer = AudioSystem.getMixer(mw.getMixerInfo());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not set mixer", e);
			mixer = null;
		}
	}

	/**
	 * Gets the volume.
	 *
	 * @return The current volume.
	 */
	public int getVolume() {
		return volume;
	}

	/**
	 * Sets the volume.
	 *
	 * @param volume
	 *            the new volume
	 */
	private void setVolume(int volume) {
		this.volume = volume;
	}

	/**
	 * Plays a file once.
	 *
	 * @param file
	 *            The <code>File</code> to be played.
	 */
	public void playOnce(File file) {
		if (getMixer() == null)
			return; // Fail faster.
		try {
			soundPlayerThread.add(getAudioInputStream(file));
			soundPlayerThread.awaken();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Unable to play: " + file.getName(), e);
		}
	}

	/**
	 * Stops the current sound.
	 */
	public void stop() {
		soundPlayerThread.stopPlaying();
		soundPlayerThread.awaken();
	}

	/**
	 * Thread for playing sound files.
	 */
	private class SoundPlayerThread extends Thread {

		/** The Constant BUFSIZ. */
		private static final int BUFSIZ = 8192;

		/** The data. */
		private final byte[] data = new byte[BUFSIZ];

		/** The play list. */
		private final List<AudioInputStream> playList = new ArrayList<>();

		/** The play done. */
		private boolean playDone = true;

		/**
		 * Instantiates a new sound player thread.
		 */
		public SoundPlayerThread() {
			super(FreeCol.CLIENT_THREAD + "SoundPlayer");
		}

		/**
		 * Awaken.
		 */
		private synchronized void awaken() {
			notify();
		}

		/**
		 * Go to sleep.
		 *
		 * @throws InterruptedException
		 *             the interrupted exception
		 */
		private synchronized void goToSleep() throws InterruptedException {
			wait();
		}

		/**
		 * Keep playing.
		 *
		 * @return true, if successful
		 */
		public synchronized boolean keepPlaying() {
			return !playDone;
		}

		/**
		 * Start playing.
		 */
		public synchronized void startPlaying() {
			playDone = false;
		}

		/**
		 * Stop playing.
		 */
		public synchronized void stopPlaying() {
			playDone = true;
		}

		/**
		 * Adds the.
		 *
		 * @param ais
		 *            the ais
		 */
		public synchronized void add(AudioInputStream ais) {
			playList.add(ais);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			for (;;) {
				if (playList.isEmpty()) {
					try {
						goToSleep();
					} catch (InterruptedException e) {
						continue;
					}
				} else {
					try (AudioInputStream in = playList.remove(0);) {
						playSound(in);
					} catch (IOException e) {
						logger.log(Level.WARNING, "Failure playing audio.", e);
					}
				}
			}
		}

		/**
		 * Sleep.
		 *
		 * @param t
		 *            the t
		 */
		private void sleep(int t) {
			try {
				Thread.sleep(t);
			} catch (InterruptedException e) {
			}
		}

		/**
		 * Sets the volume.
		 *
		 * @param line
		 *            the line
		 * @param vol
		 *            the vol
		 */
		private void setVolume(SourceDataLine line, int vol) {
			FloatControl.Type type = (line.isControlSupported(FloatControl.Type.VOLUME)) ? FloatControl.Type.VOLUME
					: (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) ? FloatControl.Type.MASTER_GAIN : null;
			if (type == null) {
				logger.warning("No volume or master gain controls.");
				return;
			}
			FloatControl control;
			try {
				control = (FloatControl) line.getControl(type);
			} catch (IllegalArgumentException e) {
				return; // Should not happen
			}
			//
			// The units of MASTER_GAIN seem to consistently be dB, but
			// in the case of VOLUME this is unclear (there is even a query
			// to that effect in the source). getUnits() says "pulseaudio
			// units" on my boxen, and the PulseAudio doco talks about dB
			// so for now we are assuming that the controls we are using
			// are both logarithmic:
			//
			// gain = A.log_10(k.vol)
			// So scale vol <= 1 to gain_min and vol >= 100 to gain_max
			// gain_min = A.log_10(k.1)
			// gain_max = A.log_10(k.100)
			// Solving for A,k yields:
			// A = (gain_max - gain_min)/2
			// k = 10^(gain_min/A)
			// =>
			// gain = gain_min + (gain_max - gain_min)/2 * log_10(vol)
			//
			float min = control.getMinimum();
			float max = control.getMaximum();
			float gain = (vol <= 0) ? min : (vol >= 100) ? max : min + 0.5f * (max - min) * (float) Math.log10(vol);
			try {
				control.setValue(gain);
				logger.finest("Using volume " + vol + "%, " + control.getUnits() + "=" + gain + " control=" + type);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not set volume " + " (control=" + type + " in [" + min + "," + max
						+ "])" + " to " + gain + control.getUnits(), e);
			}
		}

		/**
		 * Open line.
		 *
		 * @param audioFormat
		 *            the audio format
		 * @return the source data line
		 */
		private SourceDataLine openLine(AudioFormat audioFormat) {
			SourceDataLine line = null;
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
			if (!mixer.isLineSupported(info)) {
				logger.log(Level.WARNING, "Mixer does not support " + info);
				return null;
			}
			try {
				line = (SourceDataLine) mixer.getLine(info);
				line.open(audioFormat, BUFSIZ);
				line.start();
				setVolume(line, volume);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Can not open SourceDataLine", e);
				return null;
			}
			return line;
		}

		/**
		 * Play a sound.
		 *
		 * @param in
		 *            The <code>AudioInputStream</code> to play.
		 * @return True if the stream was played without incident.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private boolean playSound(AudioInputStream in) throws IOException {
			boolean ret = false;

			SourceDataLine line = openLine(in.getFormat());
			if (line == null)
				return false;
			try {
				startPlaying();
				int rd;
				while (keepPlaying() && (rd = in.read(data)) > 0) {
					line.write(data, 0, rd);
				}
				ret = true;
			} finally {
				stopPlaying();
				line.drain();
				line.stop();
				line.close();
			}
			return ret;
		}
	}
}
