import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;
import java.awt.GridLayout;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

public class WeatherForecastApp {
    private static Map<String, WeatherData> dataCache = new HashMap<>();

    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            FileInputStream input = new FileInputStream("src\\config.properties");
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to load configuration.");
            return;
        }

        String apiKey = properties.getProperty("api_key");

        try {
            String cityName = JOptionPane.showInputDialog("Enter the name of the city:");

            if (cityName != null && !cityName.isEmpty()) {
                WeatherData cachedData = dataCache.get(cityName);

                if (cachedData != null && isCacheValid(cachedData)) {
                    displayWeatherData(cachedData);
                } else {
                    WeatherData newData = fetchWeatherData(apiKey, cityName);
                    if (newData != null) {
                        dataCache.put(cityName, newData);
                        displayWeatherData(newData);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "City name cannot be empty.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isCacheValid(WeatherData data) {
        long currentTime = System.currentTimeMillis() / 1000;
        return (currentTime - data.getTimestamp()) <= 3600; // Cache data for 1 hour
    }

    private static WeatherData fetchWeatherData(String apiKey, String cityName) {
        try {
            URL currentWeatherURL = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&APPID=" + apiKey);
            HttpURLConnection currentWeatherConn = (HttpURLConnection) currentWeatherURL.openConnection();
            currentWeatherConn.setRequestMethod("GET");
            BufferedReader currentWeatherReader = new BufferedReader(new InputStreamReader(currentWeatherConn.getInputStream()));
            String currentWeatherLine;
            StringBuilder currentWeatherResult = new StringBuilder();
            while ((currentWeatherLine = currentWeatherReader.readLine()) != null) {
                currentWeatherResult.append(currentWeatherLine);
            }
            currentWeatherReader.close();

            JSONObject currentWeatherJson = new JSONObject(currentWeatherResult.toString());
            double tempKelvin = currentWeatherJson.getJSONObject("main").getDouble("temp");
            String condition = currentWeatherJson.getJSONArray("weather").getJSONObject(0).getString("description");

            double tempFahrenheit = (tempKelvin - 273.15) * 9/5 + 32;

            URL forecastURL = new URL("http://api.openweathermap.org/data/2.5/forecast?q=" + cityName + "&APPID=" + apiKey);
            HttpURLConnection forecastConn = (HttpURLConnection) forecastURL.openConnection();
            forecastConn.setRequestMethod("GET");
            BufferedReader forecastReader = new BufferedReader(new InputStreamReader(forecastConn.getInputStream()));
            String forecastLine;
            StringBuilder forecastResult = new StringBuilder();
            while ((forecastLine = forecastReader.readLine()) != null) {
                forecastResult.append(forecastLine);
            }
            forecastReader.close();

            JSONObject forecastJson = new JSONObject(forecastResult.toString());

            WeatherData weatherData = new WeatherData(tempFahrenheit, condition, forecastJson, System.currentTimeMillis() / 1000);

            return weatherData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void displayWeatherData(WeatherData data) {
        JFrame frame = new JFrame("Weather Forecast");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(1, 1));

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        String imagePath;
        if (month >= Calendar.MARCH && month <= Calendar.MAY) {
            imagePath = "images\\spring.jpg";
        } else if (month >= Calendar.JUNE && month <= Calendar.AUGUST) {
            imagePath = "images\\summer.jpg";
        } else if (month >= Calendar.SEPTEMBER && month <= Calendar.NOVEMBER) {
            imagePath = "images\\fall.jpg";
        } else {
            imagePath = "images\\winter.jpg";
        }
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            ImageIcon backgroundIcon = new ImageIcon(image);
            JLabel backgroundLabel = new JLabel(backgroundIcon);

            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
            double tempFahrenheitRounded = Math.round(data.getTemperature());
            JTextArea currentWeatherText = new JTextArea("Current Temperature: " + (int) tempFahrenheitRounded + " °F\nCondition: " + data.getCondition());

            JTextArea forecastTextArea = new JTextArea("5-Day Forecast:\n");
            for (int i = 0; i < data.getForecastList().length(); i += 8) {
                JSONObject forecastData = data.getForecastList().getJSONObject(i);
                long timestamp = forecastData.getLong("dt");
                double forecastTemp = forecastData.getJSONObject("main").getDouble("temp");
                String forecastCondition = forecastData.getJSONArray("weather").getJSONObject(0).getString("description");

                double forecastTempFahrenheit = (forecastTemp - 273.15) * 9 / 5 + 32;

                String forecastDate = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
                        .format(new java.util.Date(timestamp * 1000));

                forecastTextArea.append("\n" + forecastDate + ": " + (int) forecastTempFahrenheit + " °F, " + forecastCondition);
            }

            infoPanel.add(currentWeatherText);
            infoPanel.add(new JScrollPane(forecastTextArea));

            frame.add(backgroundLabel);
            frame.add(infoPanel);

            frame.setSize(800, 600);
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class WeatherData {
    private double temperature;
    private String condition;
    private JSONArray forecastList;
    private long timestamp;

    public WeatherData(double temperature, String condition, JSONObject forecastJson, long timestamp) {
        this.temperature = temperature;
        this.condition = condition;
        this.forecastList = forecastJson.getJSONArray("list");
        this.timestamp = timestamp;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getCondition() {
        return condition;
    }

    public JSONArray getForecastList() {
        return forecastList;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
