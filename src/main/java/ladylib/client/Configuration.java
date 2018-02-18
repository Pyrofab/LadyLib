package ladylib.client;

/**
 * TODO that won't do, we need a proper assisted configuration system
 */
public class Configuration {
    public static Client client = new Client();

    public static class Client {
        public int maxParticles = 500;
    }
}
