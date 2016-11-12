import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBProperties {
    private String url;
    private String user;
    private String password;

    public DBProperties() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties");
        Properties props = new Properties();

        try {
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        url = props.getProperty("db.url");
        user = props.getProperty("db.user");
        password = props.getProperty("db.password");
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
