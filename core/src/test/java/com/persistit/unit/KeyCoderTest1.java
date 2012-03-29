/**
 * END USER LICENSE AGREEMENT (“EULA”)
 *
 * READ THIS AGREEMENT CAREFULLY (date: 9/13/2011):
 * http://www.akiban.com/licensing/20110913
 *
 * BY INSTALLING OR USING ALL OR ANY PORTION OF THE SOFTWARE, YOU ARE ACCEPTING
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. YOU AGREE THAT THIS
 * AGREEMENT IS ENFORCEABLE LIKE ANY WRITTEN AGREEMENT SIGNED BY YOU.
 *
 * IF YOU HAVE PAID A LICENSE FEE FOR USE OF THE SOFTWARE AND DO NOT AGREE TO
 * THESE TERMS, YOU MAY RETURN THE SOFTWARE FOR A FULL REFUND PROVIDED YOU (A) DO
 * NOT USE THE SOFTWARE AND (B) RETURN THE SOFTWARE WITHIN THIRTY (30) DAYS OF
 * YOUR INITIAL PURCHASE.
 *
 * IF YOU WISH TO USE THE SOFTWARE AS AN EMPLOYEE, CONTRACTOR, OR AGENT OF A
 * CORPORATION, PARTNERSHIP OR SIMILAR ENTITY, THEN YOU MUST BE AUTHORIZED TO SIGN
 * FOR AND BIND THE ENTITY IN ORDER TO ACCEPT THE TERMS OF THIS AGREEMENT. THE
 * LICENSES GRANTED UNDER THIS AGREEMENT ARE EXPRESSLY CONDITIONED UPON ACCEPTANCE
 * BY SUCH AUTHORIZED PERSONNEL.
 *
 * IF YOU HAVE ENTERED INTO A SEPARATE WRITTEN LICENSE AGREEMENT WITH AKIBAN FOR
 * USE OF THE SOFTWARE, THE TERMS AND CONDITIONS OF SUCH OTHER AGREEMENT SHALL
 * PREVAIL OVER ANY CONFLICTING TERMS OR CONDITIONS IN THIS AGREEMENT.
 */

package com.persistit.unit;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

import com.persistit.Key;
import com.persistit.encoding.CoderContext;
import com.persistit.encoding.KeyCoder;
import com.persistit.encoding.KeyRenderer;
import com.persistit.encoding.KeyStringCoder;
import com.persistit.exception.ConversionException;

public class KeyCoderTest1 extends PersistitUnitTestCase {


    @Test
    public void test1() {
        System.out.print("test1 ");
        final KeyStringCoder coder = new TestStringCoder();
        Key key1;
        Key key2;
        key1 = new Key(_persistit);
        key2 = new Key(_persistit);
        key1.setKeyStringCoder(coder);
        key2.setKeyStringCoder(coder);

        final String a1 = "Abcde";
        final String b1 = "abCDE";
        final String c1 = "Bcde";

        key1.clear().append(a1);
        key2.clear().append(b1);

        final String a2 = key1.indexTo(0).decodeString();
        final String b2 = key2.indexTo(0).decodeString();

        assertEquals(a1, a2);
        assertEquals(b1, b2);
        assertTrue(key1.compareTo(key2) < 0);

        key1.clear().append(c1);
        final String c2 = (String) key1.indexTo(0).decode();
        assertTrue(key1.compareTo(key2) > 0);
        assertEquals(c1, c2);

        System.out.println("- done");
    }

    @Test
    public void test2() throws MalformedURLException {
        System.out.print("test2 ");
        final KeyCoder coder = new TestKeyRenderer();
        _persistit.getCoderManager().registerKeyCoder(URL.class, coder);
        Key key1;
        Key key2;

        key1 = new Key(_persistit);
        final URL url1 = new URL("http://w/z");
        final URL url2 = new URL("http://w:8080/z?userid=pb");
        key1.clear();
        key1.append("a");
        key1.append(url1);
        key1.append("b");
        key1.append(url2);
        key1.append("c");

        key1.reset();
        final String a = key1.decodeString();
        final Object obj1 = key1.decode();
        final String b = key1.decodeString();
        final Object obj2 = key1.decode();
        final String c = key1.decodeString();

        assertEquals("a", a);
        assertEquals("b", b);
        assertEquals("c", c);
        assertEquals(url1.toString(), obj1.toString());
        assertEquals(url2.toString(), obj2.toString());

        final StringBuilder sb = new StringBuilder();
        key1.indexTo(1);
        key1.decode(sb);
        assertEquals(url1.toString(), sb.toString());

        sb.setLength(0);
        key1.indexTo(3);
        key1.decode(sb);
        assertEquals(url2.toString(), sb.toString());

        key1.reset();
        final String toString = key1.toString();
        assertEquals(toString, "{\"a\",(java.net.URL){\"http\",\"w\",-1,\"/z\"},\"b\","
                + "(java.net.URL){\"http\",\"w\",8080,\"/z?userid=pb\"},\"c\"}");
        System.out.println("- done");
    }

    public static void main(final String[] args) throws Exception {
        new KeyCoderTest1().initAndRunTest();
    }

    @Override
    public void runAllTests() throws Exception {
        test1();
        test2();
    }

    public void debugAssert(boolean condition) {
        Assert.assertTrue(condition);
    }

    public static class TestKeyRenderer implements KeyRenderer {

        @Override
        public void appendKeySegment(final Key key, final Object object, final CoderContext context) {
            final URL url = (URL) object;
            key.append(url.getProtocol());
            key.append(url.getHost());
            key.append(url.getPort());
            key.append(url.getFile());
        }

        @Override
        public Object decodeKeySegment(final Key key, final Class cl, final CoderContext context) {
            final String protocol = key.decodeString();
            final String host = key.decodeString();
            final int port = key.decodeInt();
            final String file = key.decodeString();
            try {
                return new URL(protocol, host, port, file);
            } catch (final MalformedURLException mue) {
                throw new ConversionException(mue);
            }
        }

        @Override
        public void renderKeySegment(final Key key, final Object target, final Class cl, final CoderContext context) {
            final StringBuilder sb = (StringBuilder) target;
            key.decodeString(sb);
            sb.append("://");
            key.decodeString(sb);
            final int port = key.decodeInt();
            if (port != -1) {
                sb.append(':');
                sb.append(port);
            }
            key.decodeString(sb);
        }
    }

    public static class TestStringCoder implements KeyStringCoder {
        @Override
        public void appendKeySegment(final Key key, final Object object, final CoderContext context) {
            if (object instanceof CharSequence) {
                final CharSequence s = (CharSequence) object;
                final byte[] bytes = key.getEncodedBytes();
                final int start = key.getEncodedSize();
                int end = start;
                for (int i = 0; i < s.length(); i++) {
                    int ch = s.charAt(i);
                    if ((ch >= 'A') && (ch < 'M')) {
                        ch = 'A' + 2 * (ch - 'A');
                    } else if ((ch >= 'M') && (ch <= 'Z')) {
                        ch = 'a' + 2 * (ch - 'M');
                    } else if ((ch >= 'a') && (ch < 'm')) {
                        ch = 'B' + 2 * (ch - 'a');
                    } else if ((ch >= 'm') && (ch <= 'z')) {
                        ch = 'b' + 2 * (ch - 'm');
                    }
                    bytes[end++] = (byte) ch;
                }
                key.setEncodedSize(end);
            }
        }

        @Override
        public Object decodeKeySegment(final Key key, final Class cl, final CoderContext context) {
            final StringBuilder sb = new StringBuilder();
            renderKeySegment(key, sb, cl, context);
            if (cl == StringBuilder.class) {
                return sb;
            }
            if (cl == String.class) {
                return sb.toString();
            }
            throw new ConversionException("String conversion to class " + cl.getName() + " is not supported");
        }

        @Override
        public void renderKeySegment(final Key key, final Object target, final Class cl, final CoderContext context) {
            final StringBuilder sb = (StringBuilder) target;
            final byte[] bytes = key.getEncodedBytes();
            final int start = key.getIndex();
            for (int end = start;; end++) {
                int ch = bytes[end];
                if (ch == 0) {
                    key.setIndex(end);
                    break;
                }
                final boolean lower = (ch & 1) == 0;
                if ((ch >= 'A') && (ch <= 'Z')) {
                    ch = 'A' + (ch - 'A') / 2;
                    if (lower) {
                        ch += ('a' - 'A');
                    }
                } else if ((ch >= 'a') && (ch <= 'z')) {
                    ch = 'M' + (ch - 'a') / 2;
                    if (lower) {
                        ch += ('a' - 'A');
                    }
                }
                sb.append((char) ch);
            }
        }
    }
}
