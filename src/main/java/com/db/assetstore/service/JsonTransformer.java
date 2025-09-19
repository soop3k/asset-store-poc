package com.db.assetstore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.spi.Language;
import org.apache.camel.Expression;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * JSON -> JSON transformer powered by templates loaded from the classpath.
 *
 * Implementation uses Apache Camel JSLT under-the-hood via a small engine abstraction,
 * making it easy to swap the library in the future.
 *
 * Add a new transform by dropping:
 *  - transforms/{name}.jslt  (Camel JSLT template)
 * Optionally, provide an output JSON Schema at schemas/transforms/{name}.schema.json
 * to have the result validated automatically.
 */
public final class JsonTransformer {
    private static final ObjectMapper M = new ObjectMapper();

    private final TransformEngine engine;

    public JsonTransformer() {
        this.engine = new CamelJsltTransformEngine();
    }

    public JsonTransformer(TransformEngine engine) {
        this.engine = Objects.requireNonNull(engine);
    }

    /**
     * Apply the named transform to the provided JSON string and return the JSON result.
     * @param transformName logical name of the transform ("transforms/{name}.jslt")
     * @param inputJson canonical input JSON to transform
     * @return transformed JSON string
     */
    public String transform(String transformName, String inputJson) {
        Objects.requireNonNull(transformName, "transformName");
        Objects.requireNonNull(inputJson, "inputJson");
        String jsltPath = "transforms/" + transformName + ".jslt";
        String schemaPath = "schemas/transforms/" + transformName + ".schema.json";
        try {
            if (!resourceExists(jsltPath)) {
                throw new IllegalArgumentException("Transform template not found: " + jsltPath);
            }
            String json = engine.applyTemplate(jsltPath, inputJson);
            // Validate against schema if present
            JsonSchemaValidator.validateIfPresent(json, schemaPath);
            return json;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to transform JSON: " + e.getMessage(), e);
        }
    }

    private static boolean resourceExists(String path) {
        return JsonTransformer.class.getClassLoader().getResource(path) != null;
    }

    // --- Engine abstraction ---
    public interface TransformEngine {
        String applyTemplate(String classpathTemplate, String inputJson) throws IOException;
    }

    // Fallback engine using the standalone JSLT library
    static final TransformEngine JSLT_FALLBACK = new JsltLibraryEngine();

    static final class JsltLibraryEngine implements TransformEngine {
        @Override
        public String applyTemplate(String classpathTemplate, String inputJson) throws IOException {
            String template = CamelJsltTransformEngine.readResourceAsString(classpathTemplate);
            try {
                com.fasterxml.jackson.databind.JsonNode in = M.readTree(inputJson);
                com.schibsted.spt.data.jslt.Expression expr = com.schibsted.spt.data.jslt.Parser.compileString(template);
                com.fasterxml.jackson.databind.JsonNode out = expr.apply(in);
                return M.writeValueAsString(out);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to apply JSLT (fallback engine): " + e.getMessage(), e);
            }
        }
    }

    /**
     * Apache Camel JSLT engine implementation.
     */
    static final class CamelJsltTransformEngine implements TransformEngine {
        private static volatile CamelContext CAMEL;

        @Override
        public String applyTemplate(String classpathTemplate, String inputJson) throws IOException {
            ensureCamel();
            String template = readResourceAsString(classpathTemplate);
            try {
                Language lang;
                try {
                    Class<?> cls = Class.forName("org.apache.camel.language.jslt.JsltLanguage");
                    Object inst = cls.getDeclaredConstructor().newInstance();
                    lang = (Language) inst;
                } catch (ClassNotFoundException cnf) {
                    // Fallback to pure JSLT engine when Camel JSLT language is not available
                    return JSLT_FALLBACK.applyTemplate(classpathTemplate, inputJson);
                }
                Expression expr = lang.createExpression(template);
                Exchange ex = new DefaultExchange(CAMEL);
                ex.getMessage().setBody(M.readTree(inputJson)); // provide JsonNode as input
                Object out = expr.evaluate(ex, Object.class);
                return M.writeValueAsString(out);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to apply JSLT: " + e.getMessage(), e);
            }
        }

        private static void ensureCamel() {
            if (CAMEL == null) {
                synchronized (JsonTransformer.class) {
                    if (CAMEL == null) {
                        DefaultCamelContext ctx = new DefaultCamelContext();
                        try {
                            ctx.start();
                        } catch (Exception e) {
                            throw new IllegalStateException("Failed to start Camel context", e);
                        }
                        CAMEL = ctx;
                    }
                }
            }
        }

        private static String readResourceAsString(String path) throws IOException {
            ClassLoader cl = JsonTransformer.class.getClassLoader();
            try (InputStream is = cl.getResourceAsStream(path)) {
                if (is == null) {
                    throw new IllegalArgumentException("Transform template not found: " + path);
                }
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }
}
