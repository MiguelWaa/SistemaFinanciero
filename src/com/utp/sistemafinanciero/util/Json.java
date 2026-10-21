package com.utp.sistemafinanciero.util;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilidad minima para convertir objetos Java a JSON y viceversa, sin
 * librerias externas (para que el proyecto no necesite dependencias
 * adicionales aparte del driver de PostgreSQL).
 *
 * <ul>
 *   <li>{@link #stringify(Object)} serializa Map, List, arreglos, tipos
 *       basicos, {@link BigDecimal}, {@link LocalDate}/{@link LocalDateTime}
 *       y cualquier POJO (leyendo sus metodos getX()/isX()).</li>
 *   <li>{@link #parse(String)} analiza texto JSON y devuelve estructuras
 *       {@link Map}/{@link List}/String/Double/Boolean/null.</li>
 * </ul>
 */
public final class Json {

    private Json() {
        // Clase de utilidad.
    }

    // ====================== Serializacion ======================

    public static String stringify(Object valor) {
        StringBuilder sb = new StringBuilder();
        write(valor, sb);
        return sb.toString();
    }

    private static void write(Object v, StringBuilder sb) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String || v instanceof Character) {
            quote(v.toString(), sb);
        } else if (v instanceof Boolean) {
            sb.append(v.toString());
        } else if (v instanceof BigDecimal) {
            sb.append(((BigDecimal) v).toPlainString());
        } else if (v instanceof Number) {
            sb.append(v.toString());
        } else if (v instanceof LocalDate || v instanceof LocalDateTime) {
            quote(v.toString(), sb);
        } else if (v instanceof Map) {
            writeMap((Map<?, ?>) v, sb);
        } else if (v instanceof Iterable) {
            writeIterable((Iterable<?>) v, sb);
        } else if (v instanceof Object[]) {
            writeIterable(java.util.Arrays.asList((Object[]) v), sb);
        } else {
            writePojo(v, sb);
        }
    }

    private static void writeMap(Map<?, ?> map, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            quote(String.valueOf(e.getKey()), sb);
            sb.append(':');
            write(e.getValue(), sb);
        }
        sb.append('}');
    }

    private static void writeIterable(Iterable<?> it, StringBuilder sb) {
        sb.append('[');
        boolean first = true;
        for (Object o : it) {
            if (!first) sb.append(',');
            first = false;
            write(o, sb);
        }
        sb.append(']');
    }

    /** Serializa un POJO leyendo sus getters (getX / isX sin argumentos). */
    private static void writePojo(Object obj, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        Method[] metodos = obj.getClass().getMethods();
        for (Method m : metodos) {
            if (m.getParameterCount() != 0) continue;
            String nombre = m.getName();
            String prop = null;
            if (nombre.startsWith("get") && nombre.length() > 3 && !nombre.equals("getClass")) {
                prop = decapitalizar(nombre.substring(3));
            } else if (nombre.startsWith("is") && nombre.length() > 2
                    && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
                prop = decapitalizar(nombre.substring(2));
            }
            if (prop == null) continue;
            Object valor;
            try {
                valor = m.invoke(obj);
            } catch (ReflectiveOperationException ex) {
                continue;
            }
            if (!first) sb.append(',');
            first = false;
            quote(prop, sb);
            sb.append(':');
            write(valor, sb);
        }
        sb.append('}');
    }

    private static String decapitalizar(String s) {
        if (s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private static void quote(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    // ====================== Parseo ======================

    public static Object parse(String texto) {
        return new Parser(texto).parseCompleto();
    }

    /** Parsea el texto y lo devuelve como Map (o mapa vacio si no aplica). */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String texto) {
        Object o = parse(texto);
        if (o instanceof Map) {
            return (Map<String, Object>) o;
        }
        return new LinkedHashMap<>();
    }

    private static final class Parser {
        private final String s;
        private int i;

        Parser(String s) { this.s = s == null ? "" : s; }

        Object parseCompleto() {
            skipWs();
            if (i >= s.length()) return null;
            Object v = parseValue();
            return v;
        }

        private Object parseValue() {
            skipWs();
            char c = peek();
            switch (c) {
                case '{': return parseObject();
                case '[': return parseArray();
                case '"': return parseString();
                case 't': case 'f': return parseBoolean();
                case 'n': parseLiteral("null"); return null;
                default:  return parseNumber();
            }
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{');
            skipWs();
            if (peek() == '}') { i++; return map; }
            while (true) {
                skipWs();
                String clave = parseString();
                skipWs();
                expect(':');
                Object valor = parseValue();
                map.put(clave, valor);
                skipWs();
                char c = next();
                if (c == ',') continue;
                if (c == '}') break;
                throw error("Se esperaba ',' o '}'");
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> lista = new ArrayList<>();
            expect('[');
            skipWs();
            if (peek() == ']') { i++; return lista; }
            while (true) {
                lista.add(parseValue());
                skipWs();
                char c = next();
                if (c == ',') continue;
                if (c == ']') break;
                throw error("Se esperaba ',' o ']'");
            }
            return lista;
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = next();
                if (c == '"') break;
                if (c == '\\') {
                    char e = next();
                    switch (e) {
                        case '"':  sb.append('"');  break;
                        case '\\': sb.append('\\'); break;
                        case '/':  sb.append('/');  break;
                        case 'n':  sb.append('\n'); break;
                        case 'r':  sb.append('\r'); break;
                        case 't':  sb.append('\t'); break;
                        case 'b':  sb.append('\b'); break;
                        case 'f':  sb.append('\f'); break;
                        case 'u':
                            String hex = s.substring(i, i + 4);
                            i += 4;
                            sb.append((char) Integer.parseInt(hex, 16));
                            break;
                        default: throw error("Escape invalido: \\" + e);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Object parseNumber() {
            int inicio = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) {
                i++;
            }
            String num = s.substring(inicio, i);
            if (num.isEmpty()) throw error("Numero invalido");
            return Double.parseDouble(num);
        }

        private Boolean parseBoolean() {
            if (peek() == 't') { parseLiteral("true"); return Boolean.TRUE; }
            parseLiteral("false");
            return Boolean.FALSE;
        }

        private void parseLiteral(String lit) {
            if (!s.startsWith(lit, i)) throw error("Se esperaba '" + lit + "'");
            i += lit.length();
        }

        private void skipWs() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        private char peek() {
            if (i >= s.length()) throw error("Fin de texto inesperado");
            return s.charAt(i);
        }

        private char next() {
            if (i >= s.length()) throw error("Fin de texto inesperado");
            return s.charAt(i++);
        }

        private void expect(char c) {
            char x = next();
            if (x != c) throw error("Se esperaba '" + c + "' pero se encontro '" + x + "'");
        }

        private RuntimeException error(String msg) {
            return new RuntimeException("JSON invalido en posicion " + i + ": " + msg);
        }
    }

    // ====================== Ayudas para construir respuestas ======================

    /** Crea un Map ordenado a partir de pares clave/valor: map("a",1,"b",2). */
    public static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int k = 0; k + 1 < kv.length; k += 2) {
            m.put(String.valueOf(kv[k]), kv[k + 1]);
        }
        return m;
    }

    // --- Conversiones seguras desde un Map parseado ---

    public static String str(Map<String, Object> m, String clave) {
        Object v = m.get(clave);
        return v == null ? null : v.toString();
    }

    public static Integer intOrNull(Map<String, Object> m, String clave) {
        Object v = m.get(clave);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        String s = v.toString().trim();
        return s.isEmpty() ? null : (int) Double.parseDouble(s);
    }

    public static BigDecimal decimalOrNull(Map<String, Object> m, String clave) {
        Object v = m.get(clave);
        if (v == null) return null;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        String s = v.toString().trim();
        return s.isEmpty() ? null : new BigDecimal(s);
    }
}
