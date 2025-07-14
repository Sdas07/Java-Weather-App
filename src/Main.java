import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class Main {

    private static final String API_KEY = "your_new_api_key"; 
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";

    public static void main(String[] args) {
        JFrame frame = new JFrame("Weather App");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());

        // === TOP PANEL ===
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 30));

        JTextField cityField = new JTextField(20);
        JButton getWeatherButton = new JButton("Get Weather");

        Font font = new Font("Arial", Font.PLAIN, 18);
        cityField.setFont(font);
        getWeatherButton.setFont(font);

        cityField.setPreferredSize(new Dimension(250, 40));
        getWeatherButton.setPreferredSize(new Dimension(160, 40));

        topPanel.add(cityField);
        topPanel.add(getWeatherButton);

        // === CENTER PANEL ===
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JLabel emojiLabel = new JLabel("");
        JLabel conditionLabel = new JLabel("");
        JLabel cityLabel = new JLabel("");
        JLabel tempLabel = new JLabel("");

        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        conditionLabel.setFont(new Font("SansSerif", Font.PLAIN, 28));
        cityLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        tempLabel.setFont(new Font("SansSerif", Font.BOLD, 54));

        for (JLabel label : new JLabel[]{emojiLabel, conditionLabel, cityLabel, tempLabel}) {
            label.setForeground(Color.WHITE);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setVisible(false); // For fade-in
        }

        // 💡 Push content lower to center the emoji better
        centerPanel.add(Box.createVerticalStrut(100));
        centerPanel.add(emojiLabel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(conditionLabel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(cityLabel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(tempLabel);

        // === ACTION LISTENER ===
        getWeatherButton.addActionListener(e -> {
            String city = cityField.getText().trim();
            if (!city.isEmpty()) {
                String data = getWeather(city);
                if (data != null) {
                    try {
                        JSONObject json = new JSONObject(data);
                        String condition = json.getJSONArray("weather").getJSONObject(0).getString("description");
                        double temp = json.getJSONObject("main").getDouble("temp");

                        String emoji = getEmoji(condition);
                        emojiLabel.setText(emoji);
                        conditionLabel.setText(capitalize(condition));
                        cityLabel.setText(city);
                        tempLabel.setText(String.format("%.1f°C", temp));

                        // Reset visibility
                        for (JLabel label : new JLabel[]{emojiLabel, conditionLabel, cityLabel, tempLabel}) {
                            label.setVisible(false);
                        }

                        fadeIn(emojiLabel, conditionLabel, cityLabel, tempLabel);

                    } catch (Exception ex) {
                        conditionLabel.setText("Error parsing data.");
                        cityLabel.setText("");
                        tempLabel.setText("");
                    }
                } else {
                    conditionLabel.setText("Error fetching data.");
                    cityLabel.setText("");
                    tempLabel.setText("");
                }
            }
        });

        backgroundPanel.add(topPanel, BorderLayout.NORTH);
        backgroundPanel.add(centerPanel, BorderLayout.CENTER);

        frame.add(backgroundPanel);
        frame.setVisible(true);
    }

    // === API FETCH ===
    private static String getWeather(String city) {
        try {
            String urlString = String.format(API_URL, city, API_KEY);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // === EMOJI MAPPING ===
    private static String getEmoji(String condition) {
        String text = condition.toLowerCase();
        if (text.contains("clear")) return "☀️";
        else if (text.contains("cloud")) return "☁️";
        else if (text.contains("rain") || text.contains("drizzle")) return "🌧️";
        else if (text.contains("thunder")) return "⛈️";
        else if (text.contains("snow")) return "❄️";
        else if (text.contains("mist") || text.contains("fog")) return "🌫️";
        else return "🌈";
    }

    private static String capitalize(String text) {
        if (text == null || text.length() == 0) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    // === FADE-IN LOGIC ===
    private static void fadeIn(JLabel... labels) {
        Timer timer = new Timer(200, null);
        final int[] index = {0};

        timer.addActionListener(e -> {
            if (index[0] < labels.length) {
                labels[index[0]].setVisible(true);
                index[0]++;
            } else {
                timer.stop();
            }
        });
        timer.start();
    }

    // === BACKGROUND PANEL ===
    static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel() {
            try {
                backgroundImage = new ImageIcon("background.jpg").getImage();
            } catch (Exception e) {
                System.out.println("Background image not found.");
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null)
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
