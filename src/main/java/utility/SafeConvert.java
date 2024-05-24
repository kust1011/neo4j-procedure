package utility;

import java.math.BigInteger;

public class SafeConvert {
    public static boolean toBoolean(Object value, boolean defaultValue) {
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    public static String toString(Object value, String defaultValue) {
        if (value != null) {
            return value.toString();
        }
        return defaultValue;
    }

    public static Double toDouble(Object value, Double defaultValue) {
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    public static Long toLong(Object value, Long defaultValue) {
        if (value instanceof String) {
            try {
                return (long) Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue;
    }

    public static BigInteger toBigInteger(Object value, BigInteger defaultValue) {
        if (value instanceof String) {
            try {
                return new BigInteger((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else if (value instanceof Number) {
            return BigInteger.valueOf(((Number) value).longValue());
        }
        return defaultValue;
    }

    public static Long toTimestamp(Object value, Long defaultValue) {
        long timestamp = toLong(value, defaultValue);
    
        if (timestamp >= 0 && timestamp <= 9999999999L) {
            return timestamp;
        }
    
        if (timestamp >= 1000000000000L && timestamp <= 9999999999999L) {
            return timestamp / 1000;
        }
    
        return defaultValue;
    }
}