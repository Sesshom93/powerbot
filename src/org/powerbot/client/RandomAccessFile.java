package org.powerbot.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.powerbot.gui.BotChrome;

/**
 * @author Timer
 */
@SuppressWarnings("unused")
public class RandomAccessFile {
	private UIDManager uidData = null;
	private java.io.RandomAccessFile raf = null;
	private Client client = null;

	private byte[] data = null;
	private int offset = 0;

	public RandomAccessFile(final String name, final String mode) throws FileNotFoundException {
		if (!shouldOverride(name)) {
			raf = new java.io.RandomAccessFile(name, mode);
		}
	}

	public RandomAccessFile(final File file, final String mode) throws FileNotFoundException {
		if (!shouldOverride(file.getName())) {
			raf = new java.io.RandomAccessFile(file, mode);
		}
	}

	private boolean shouldOverride(final String filename) {
		if (filename.equals("random.dat")) {
			uidData = new UIDManager();
			return true;
		}
		return false;
	}

	private void checkData() {
		if (uidData != null) {
			client = BotChrome.getInstance().getBot().getMethodContext().getClient();
			final String accountName = client != null ? client.getCurrentUsername() : "";

			if (!uidData.getLastUsed().equals(accountName) && data != null) {
				uidData.setUID(uidData.getLastUsed(), data);
				data = uidData.getUID(accountName);
				offset = 0;
			} else if (data == null) {
				data = uidData.getUID(accountName);
				offset = 0;
			}
		}
	}

	private void saveData() {
		if (uidData != null && data != null) {
			uidData.setUID(client != null ? client.getCurrentUsername() : "", data);
			uidData.save();
		}
	}

	public void close() throws IOException {
		if (raf != null) {
			raf.close();
		}
	}

	public long length() throws IOException {
		checkData();

		if (data != null) {
			return data.length;
		}

		return raf.length();
	}

	public int read() {
		try {
			checkData();

			if (data != null) {
				if (data.length <= offset) {
					return -1;
				}

				return 0xFF & data[offset++];
			}

			return raf.read();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return -1;
	}

	public int read(final byte[] b, final int off, int len) throws IOException {
		checkData();

		if (data != null) {
			try {
				if (b.length < off + len) {
					len = b.length - off;
				}

				if (data.length < offset + len) {
					len = data.length - offset;
				}

				if (len <= 0) {
					return -1;
				}

				for (int i = 0; i < len; i++) {
					b[off + i] = data[offset++];
				}

				return len;
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		return raf.read(b, off, len);
	}

	public void seek(final long pos) throws IOException {
		checkData();

		if (pos < 0) {
			throw new IOException("pos < 0");
		}

		if (data != null) {
			offset = (int) pos;
		} else {
			raf.seek(pos);
		}
	}

	public void write(final byte[] b, final int off, int len) throws IOException {
		checkData();

		if (data != null) {
			if (b.length < off + len) {
				len = b.length - off;
			}

			if (len <= 0) {
				return;
			}

			if (data.length < offset + len) {
				final byte[] tmp = data;
				data = new byte[offset + len];
				System.arraycopy(tmp, 0, data, 0, (offset <= tmp.length ? offset : tmp.length));
			}

			for (int i = 0; i < len; i++) {
				data[offset++] = b[off + i];
			}

			saveData();
		} else {
			raf.write(b, off, len);
		}
	}

	public void write(final int b) throws IOException {
		checkData();

		if (data != null) {
			if (data.length < offset + 1) {
				final byte[] tmp = data;
				data = new byte[offset + 1];
				System.arraycopy(tmp, 0, data, 0, (offset <= tmp.length ? offset : tmp.length));
			}

			data[offset++] = (byte) b;
			saveData();
		} else {
			raf.write(b);
		}
	}
}