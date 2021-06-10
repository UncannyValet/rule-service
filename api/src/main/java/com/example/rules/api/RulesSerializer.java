package com.example.rules.api;

import com.daxtechnologies.oam.ILogger;
import com.daxtechnologies.oam.TheLogger;
import com.daxtechnologies.util.StreamUtils;
import com.daxtechnologies.util.StringUtils;
import com.daxtechnologies.util.serialize.SerializeFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.spirent.cem.rules.api.ErrorNumbers.*;

public class RulesSerializer {

    private static final Logger LOG = LogManager.getLogger(RulesSerializer.class);
    private static final XStream xstream;

    private static final SerializeFactory<?> serializer = SerializeFactory.getXmlInstance();

    private RulesSerializer() {
    }

    /**
     * Serializes an Object to an XML string
     *
     * @param o the Object to serialize
     * @return an XML representation of the serialized Object
     */
    public static String serializeAsString(Object o) {
        return serializer.serializeAsString(o);
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
                return StringUtils.decompressText(new ByteArrayInputStream(bytes));
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
            InputStream inputStream = StringUtils.compressText(s);
            return StreamUtils.getInputStreamBytes(inputStream);
        } catch (IOException | RuntimeException e) {
            throw new RulesException(e, SERIALIZATION_ERROR);
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
                String s = StringUtils.decompressText(new ByteArrayInputStream(bytes));
                return deserialize(s);
            } else {
                return null;
            }
        } catch (RulesException r) {
            throw r;
        } catch (IOException | RuntimeException e) {
            throw new RulesException(e, DESERIALIZATION_ERROR);
        }
    }

    /**
     * Deserializes an Object from an XML String
     *
     * @param s an XML String for a serialized Object
     * @return the deserialized Object
     */
    public static <T> T deserialize(String s) {
        try {
            return StringUtils.isNotEmpty(s) ? serializer.deserialize(s) : null;
        } catch (RuntimeException e) {
            throw new RulesException(e, UNKNOWN_RULES_TYPE, s.length() < 128 ? s : s.substring(0, 124) + "...");
        }
    }
}
