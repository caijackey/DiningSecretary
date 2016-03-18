package com.fg114.main.qmoney.tools;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Vector;

public class ASN1InputStream extends FilterInputStream {

	private final int limit;

	static int findLimit(InputStream in) {
		if (in instanceof LimitedInputStream) {
			return ((LimitedInputStream) in).getRemaining();
		} else if (in instanceof ByteArrayInputStream) {
			return ((ByteArrayInputStream) in).available();
		}
		return Integer.MAX_VALUE;
	}

	protected ASN1InputStream(InputStream is) {
		this(is, findLimit(is));
	}

	protected ASN1InputStream(byte[] input) {
		this(new ByteArrayInputStream(input), input.length);
	}

	protected ASN1InputStream(byte[] input, boolean lazyEvaluate) {
		this(new ByteArrayInputStream(input), input.length, lazyEvaluate);
	}

	protected ASN1InputStream(InputStream input, int limit) {
		this(input, limit, false);
	}

	protected ASN1InputStream(InputStream input, int limit, boolean lazyEvaluate) {
		super(input);
		this.limit = limit;
	}

	private int readLength() throws IOException {
		return readLength(this, limit);
	}

	private BigInteger buildObject(int tag, int tagNo, int length,
			Vector<BigInteger> v) throws IOException {
		boolean isConstructed = (tag & 0x20) != 0;
		DefiniteLengthInputStream defIn = new DefiniteLengthInputStream(this,
				length);
		if (isConstructed) {
			buildDEREncodableVector(defIn, v);
			return null;
		}
		return new BigInteger(defIn.toByteArray());
	}

	protected void buildEncodableVector(Vector<BigInteger> v)
			throws IOException {
		BigInteger e = null;
		while ((e = readObject(v)) != null) {
			v.add(e);
		}
	}

	private void buildDEREncodableVector(DefiniteLengthInputStream dIn,
			Vector<BigInteger> v) throws IOException {
		new ASN1InputStream(dIn).buildEncodableVector(v);
	}

	private BigInteger readObject(Vector<BigInteger> v) throws IOException {
		int tag = read();
		if (tag <= 0) {
			return null;
		}
		int tagNo = readTagNumber(this, tag);
		int length = readLength();
		return buildObject(tag, tagNo, length, v);
	}

	private int readTagNumber(InputStream s, int tag) throws IOException {
		int tagNo = tag & 0x1f;
		if (tagNo == 0x1f) {
			tagNo = 0;
			int b = s.read();
			if ((b & 0x7f) == 0) {
				throw new IOException(
						"corrupted stream - invalid high tag number found");
			}
			while ((b >= 0) && ((b & 0x80) != 0)) {
				tagNo |= (b & 0x7f);
				tagNo <<= 7;
				b = s.read();
			}
			if (b < 0) {
				throw new EOFException("EOF found inside tag value.");
			}
			tagNo |= (b & 0x7f);
		}
		return tagNo;
	}

	private int readLength(InputStream s, int limit) throws IOException {
		int length = s.read();
		if (length < 0) {
			throw new EOFException("EOF found when length expected");
		}
		if (length == 0x80) {
			return -1; // indefinite-length encoding
		}
		if (length > 127) {
			int size = length & 0x7f;
			if (size > 4) {
				throw new IOException("DER length more than 4 bytes: " + size);
			}
			length = 0;
			for (int i = 0; i < size; i++) {
				int next = s.read();

				if (next < 0) {
					throw new EOFException("EOF found reading length");
				}

				length = (length << 8) + next;
			}
			if (length < 0) {
				throw new IOException(
						"corrupted stream - negative length found");
			}
			if (length >= limit) // after all we must have read at least 1 byte
			{
				throw new IOException(
						"corrupted stream - out of bounds length found");
			}
		}
		return length;
	}

}
