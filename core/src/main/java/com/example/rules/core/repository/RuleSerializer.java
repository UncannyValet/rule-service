package com.example.rules.core.repository;

import com.example.rules.api.RuleException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class RuleSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(RuleSerializer.class);
    private static final XStream xstream;

    static {
        xstream = new XStream();
        xstream.addPermission(AnyTypePermission.ANY);
    }

    private RuleSerializer() {
    }

    /**
     * Serializes an Object to an XML string
     *
     * @param o the Object to serialize
     * @return an XML representation of the serialized Object
     */
    public static String serializeAsString(Object o) {
        return xstream.toXML(o);
    }

    /**
     * Returns the XML representation of a compressed XML-serialized Object
     *
     * @param bytes the compressed XML
     * @return the XML representation of the serialized Object
     */
    public static String deserializeAsString(byte[] bytes) {
        if (bytes != null && bytes.length > 0) {
            try {
                return decompress(bytes);
            } catch (IOException e) {
                LOG.warn("Failed to deserialize data as String", e);
            }
        }
        return "";
    }

    /**
     * Serializes an Object to a compressed XML String
     *
     * @param o the Object to serialize
     * @return a byte array containing a compressed XML String
     */
    public static byte[] serialize(Object o) {
        try {
            String s = serializeAsString(o);
            return compress(s);
        } catch (IOException | RuntimeException e) {
            throw new RuleException("Failed to serialize object " + o.getClass().getName(), e);
        }
    }

    /**
     * Deserializes an Object from a compressed XML String
     *
     * @param bytes a byte array containing a compressed XML String
     * @return the deserialized Object
     */
    public static <T> T deserialize(byte[] bytes) {
        try {
            if (bytes != null && bytes.length > 0) {
                String s = decompress(bytes);
                return deserialize(s);
            } else {
                return null;
            }
        } catch (RuleException r) {
            throw r;
        } catch (IOException | RuntimeException e) {
            throw new RuleException("Failed to deserialize object", e);
        }
    }

    /**
     * Deserializes an Object from an XML String
     *
     * @param s an XML String for a serialized Object
     * @return the deserialized Object
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(String s) {
        try {
            return StringUtils.isNotEmpty(s) ? (T)xstream.fromXML(s) : null;
        } catch (RuntimeException e) {
            throw new RuleException("Failed to deserialize object", e);
        }
    }

    private static byte[] compress(String s) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            OutputStream out = new DeflaterOutputStream(os);
            out.write(s.getBytes(StandardCharsets.UTF_8));
            out.close();
            return os.toByteArray();
        }
    }

    private static String decompress(byte[] bytes) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            OutputStream out = new InflaterOutputStream(os);
            out.write(bytes);
            out.close();
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
