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

public class WeatherForecastApp {
    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            FileInputStream input = new FileInputStream("C:\\Users\\kaush\\IdeaProjects\\WeatherApp\\src\\config.properties");
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to load configuration.");
            return;
        }

        String apiKey = properties.getProperty("api_key");

        try {
            // Prompt the user for a city
            String cityName = JOptionPane.showInputDialog("Enter the name of the city:");

            if (cityName != null && !cityName.isEmpty()) {
                // Get current weather data
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

                // Parse current weather data
                JSONObject currentWeatherJson = new JSONObject(currentWeatherResult.toString());
                double tempKelvin = currentWeatherJson.getJSONObject("main").getDouble("temp");
                String condition = currentWeatherJson.getJSONArray("weather").getJSONObject(0).getString("description");

                // Convert temperature to Fahrenheit
                double tempFahrenheit = (tempKelvin - 273.15) * 9/5 + 32;

                // Get forecast data
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

                // Parse forecast data
                JSONObject forecastJson = new JSONObject(forecastResult.toString());
                JSONArray forecastList = forecastJson.getJSONArray("list");

                // Create GUI
                JFrame frame = new JFrame("Weather Forecast");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new GridLayout(1, 1)); // 1 row, 1 column

                // Create and set the seasonal background image to cover the entire window
                Calendar calendar = Calendar.getInstance();
                int month = calendar.get(Calendar.MONTH);
                String imagePath;
                if (month >= Calendar.MARCH && month <= Calendar.MAY) {
                    imagePath = "C:\\Users\\kaush\\IdeaProjects\\WeatherApp\\images\\spring.jpg";
                } else if (month >= Calendar.JUNE && month <= Calendar.AUGUST) {
                    imagePath = "C:\\Users\\kaush\\IdeaProjects\\WeatherApp\\images\\summer.jpg";
                } else if (month >= Calendar.SEPTEMBER && month <= Calendar.NOVEMBER) {
                    imagePath = "C:\\Users\\kaush\\IdeaProjects\\WeatherApp\\images\\fall.jpg";
                } else {
                    imagePath = "C:\\Users\\kaush\\IdeaProjects\\WeatherApp\\images\\winter.jpg";
                }
                BufferedImage image = ImageIO.read(new File(imagePath));
                ImageIcon backgroundIcon = new ImageIcon(image);
                JLabel backgroundLabel = new JLabel(backgroundIcon);

                // Create and set the current weather and forecast information
                JPanel infoPanel = new JPanel(new GridLayout(2, 1));
                double tempFahrenheitRounded = Math.round(tempFahrenheit);
                JTextArea currentWeatherText = new JTextArea("Current Temperature: " + (int) tempFahrenheitRounded + " °F\nCondition: " + condition);

                JTextArea forecastTextArea = new JTextArea("5-Day Forecast:\n");
                for (int i = 0; i < forecastList.length(); i += 8) { // Display every 24 hours (8 items per day)
                    JSONObject forecastData = forecastList.getJSONObject(i);
                    long timestamp = forecastData.getLong("dt");
                    double forecastTemp = forecastData.getJSONObject("main").getDouble("temp");
                    String forecastCondition = forecastData.getJSONArray("weather").getJSONObject(0).getString("description");

                    // Convert temperature to Fahrenheit
                    double forecastTempFahrenheit = (forecastTemp - 273.15) * 9 / 5 + 32;

                    // Format date from timestamp (you can use SimpleDateFormat for better formatting)
                    String forecastDate = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
                            .format(new java.util.Date(timestamp * 1000));

                    forecastTextArea.append("\n" + forecastDate + ": " + (int) forecastTempFahrenheit + " °F, " + forecastCondition);
                }

                infoPanel.add(currentWeatherText);
                infoPanel.add(new JScrollPane(forecastTextArea));

                // Add the background label and info panel to the frame
                frame.add(backgroundLabel);
                frame.add(infoPanel);

                frame.setSize(800, 600);
                frame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "City name cannot be empty.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
