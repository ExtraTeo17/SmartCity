package smartcity.config;

/**
 * Values overridden with properties.
 * Fields can't be final because would be inlined by JVM.
 */
public class ConfigProperties {
    public static int JADE_PORT = 4000;
    public static int WEB_PORT = 9000;
    public static String[] OVERPASS_APIS = {
            "https://overpass.openstreetmap.ru/api/interpreter",
            "https://lz4.overpass-api.de/api/interpreter",
            "https://z.overpass-api.de/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter",
    };
}
