package com.asquera.elasticsearch.plugins.http.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

public class Base64 {

    public static final Charset PREFERRED_ENCODING = Charset.forName("US-ASCII");

    private static final byte[] _STANDARD_DECODABET = new byte[]{-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, -9, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, -9, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};
    private static final byte[] _URL_SAFE_DECODABET = new byte[]{-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, 63, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};
    private static final byte[] _ORDERED_DECODABET = new byte[]{-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 0, -9, -9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -9, -9, -9, -1, -9, -9, -9, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, -9, -9, -9, -9, 37, -9, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};


    public static byte[] decode(String s) throws IOException {
        return decode(s, 0);
    }

    private static final byte[] getDecodabet(int options) {
        if ((options & 16) == 16) {
            return _URL_SAFE_DECODABET;
        } else {
            return (options & 32) == 32 ? _ORDERED_DECODABET : _STANDARD_DECODABET;
        }
    }


    public static byte[] decode(String s, int options) throws IOException {
        if (s == null) {
            throw new NullPointerException("Input string was null.");
        } else {
            byte[] bytes = s.getBytes(PREFERRED_ENCODING);
            bytes = decode(bytes, 0, bytes.length, options);
            boolean dontGunzip = (options & 4) != 0;
            if (bytes != null && bytes.length >= 4 && !dontGunzip) {
                int head = bytes[0] & 255 | bytes[1] << 8 & '\uff00';
                if (35615 == head) {
                    ByteArrayInputStream bais = null;
                    GZIPInputStream gzis = null;
                    ByteArrayOutputStream baos = null;
                    byte[] buffer = new byte[2048];
                    boolean var9 = false;

                    try {
                        baos = new ByteArrayOutputStream();
                        bais = new ByteArrayInputStream(bytes);
                        gzis = new GZIPInputStream(bais);

                        int length;
                        while((length = gzis.read(buffer)) >= 0) {
                            baos.write(buffer, 0, length);
                        }

                        bytes = baos.toByteArray();
                    } catch (IOException var27) {
                        ;
                    } finally {
                        try {
                            baos.close();
                        } catch (Exception var26) {
                            ;
                        }

                        try {
                            gzis.close();
                        } catch (Exception var25) {
                            ;
                        }

                        try {
                            bais.close();
                        } catch (Exception var24) {
                            ;
                        }

                    }
                }
            }

            return bytes;
        }
    }

    public static byte[] decode(byte[] source, int off, int len, int options) throws IOException {
        if (source == null) {
            throw new NullPointerException("Cannot decode null source array.");
        } else if (off >= 0 && off + len <= source.length) {
            if (len == 0) {
                return new byte[0];
            } else if (len < 4) {
                throw new IllegalArgumentException("Base64-encoded string must have at least four characters, but length specified was " + len);
            } else {
                byte[] DECODABET = getDecodabet(options);
                int len34 = len * 3 / 4;
                byte[] outBuff = new byte[len34];
                int outBuffPosn = 0;
                byte[] b4 = new byte[4];
                int b4Posn = 0;
//                int i = false;
                byte sbiDecode ;

                for(int i = off; i < off + len; ++i) {
                    sbiDecode = DECODABET[source[i] & 255];
                    if (sbiDecode < -5) {
                        throw new IOException(String.format(Locale.ROOT, "Bad Base64 input character decimal %d in array position %d", source[i] & 255, i));
                    }

                    if (sbiDecode >= -1) {
                        b4[b4Posn++] = source[i];
                        if (b4Posn > 3) {
                            outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn, options);
                            b4Posn = 0;
                            if (source[i] == 61) {
                                if (i + 1 < len + off) {
                                    throw new IOException(String.format(Locale.ROOT, "Found equals sign at position %d of the base64 string, not at the end", i));
                                }
                                break;
                            }
                        } else if (source[i] == 61 && len + off > i && source[i + 1] != 61) {
                            throw new IOException(String.format(Locale.ROOT, "Found equals sign at position %d of the base64 string, not at the end", i));
                        }
                    }
                }

                byte[] out = new byte[outBuffPosn];
                System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
                return out;
            }
        } else {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Source array with length %d cannot have offset of %d and process %d bytes.", source.length, off, len));
        }
    }

    private static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset, int options) {
        if (source == null) {
            throw new NullPointerException("Source array was null.");
        } else if (destination == null) {
            throw new NullPointerException("Destination array was null.");
        } else if (srcOffset >= 0 && srcOffset + 3 < source.length) {
            if (destOffset >= 0 && destOffset + 2 < destination.length) {
                byte[] DECODABET = getDecodabet(options);
                int outBuff;
                if (source[srcOffset + 2] == 61) {
                    outBuff = (DECODABET[source[srcOffset]] & 255) << 18 | (DECODABET[source[srcOffset + 1]] & 255) << 12;
                    destination[destOffset] = (byte)(outBuff >>> 16);
                    return 1;
                } else if (source[srcOffset + 3] == 61) {
                    outBuff = (DECODABET[source[srcOffset]] & 255) << 18 | (DECODABET[source[srcOffset + 1]] & 255) << 12 | (DECODABET[source[srcOffset + 2]] & 255) << 6;
                    destination[destOffset] = (byte)(outBuff >>> 16);
                    destination[destOffset + 1] = (byte)(outBuff >>> 8);
                    return 2;
                } else {
                    outBuff = (DECODABET[source[srcOffset]] & 255) << 18 | (DECODABET[source[srcOffset + 1]] & 255) << 12 | (DECODABET[source[srcOffset + 2]] & 255) << 6 | DECODABET[source[srcOffset + 3]] & 255;
                    destination[destOffset] = (byte)(outBuff >> 16);
                    destination[destOffset + 1] = (byte)(outBuff >> 8);
                    destination[destOffset + 2] = (byte)outBuff;
                    return 3;
                }
            } else {
                throw new IllegalArgumentException(String.format(Locale.ROOT, "Destination array with length %d cannot have offset of %d and still store three bytes.", destination.length, destOffset));
            }
        } else {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Source array with length %d cannot have offset of %d and still process four bytes.", source.length, srcOffset));
        }
    }


}
