package com.fg114.main.qmoney.tools;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class DefiniteLengthInputStream extends LimitedInputStream {
	private static final byte[] EMPTY_BYTES = new byte[0];

	private final int _originalLength;
	private int _remaining;

	DefiniteLengthInputStream(InputStream in, int length) {
		super(in, length);
		if (length < 0) {
			throw new IllegalArgumentException("negative lengths not allowed");
		}
		this._originalLength = length;
		this._remaining = length;
		if (length == 0) {
			setParentEofDetect(true);
		}
	}

	int getRemaining() {
		return _remaining;
	}

	public int read() throws IOException {
		if (_remaining == 0) {
			return -1;
		}

		int b = _in.read();

		if (b < 0) {
			throw new EOFException("DEF length " + _originalLength
					+ " object truncated by " + _remaining);
		}

		if (--_remaining == 0) {
			setParentEofDetect(true);
		}

		return b;
	}

	public int read(byte[] buf, int off, int len) throws IOException {
		if (_remaining == 0) {
			return -1;
		}

		int toRead = Math.min(len, _remaining);
		int numRead = _in.read(buf, off, toRead);

		if (numRead < 0) {
			throw new EOFException("DEF length " + _originalLength
					+ " object truncated by " + _remaining);
		}

		if ((_remaining -= numRead) == 0) {
			setParentEofDetect(true);
		}

		return numRead;
	}

	byte[] toByteArray() throws IOException {
		if (_remaining == 0) {
			return EMPTY_BYTES;
		}
		byte[] bytes = new byte[_remaining];
		if ((_remaining -= readFully(_in, bytes)) != 0) {
			throw new EOFException("DEF length " + _originalLength
					+ " object truncated by " + _remaining);
		}
		setParentEofDetect(true);
		return bytes;
	}

	private int readFully(InputStream inStr, byte[] buf) throws IOException {
		return readFully(inStr, buf, 0, buf.length);
	}

	private int readFully(InputStream inStr, byte[] buf, int off, int len)
			throws IOException {
		int totalRead = 0;
		while (totalRead < len) {
			int numRead = inStr.read(buf, off + totalRead, len - totalRead);
			if (numRead < 0) {
				break;
			}
			totalRead += numRead;
		}
		return totalRead;
	}
}
