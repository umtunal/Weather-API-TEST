package com.weatherapi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * OpenWeather API Test Otomasyonu - SSL Sorunları Çözümlü
 */
public class OpenWeatherAPITester {

    private static final String API_KEY = "e862540ab1126a114cd3a914bfba7abf";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final HttpClient client;
    private static final StringBuilder report = new StringBuilder();

    static {
        // SSL sertifika doğrulamasını devre dışı bırak
        client = createUnsafeHttpClient();
    }

    /**
     * SSL doğrulaması yapmayan HttpClient oluştur
     */
    private static HttpClient createUnsafeHttpClient() {
        try {
            // Tüm sertifikaları güvenilir kılan TrustManager
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            // SSL context'i güvenilir kıl
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("SSL HttpClient oluşturulamadı", e);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== OPENWEATHER API TEST OTOMASYONU BAŞLIYOR ===");
        System.out.println("⚠️  SSL Doğrulama: DEVRE DIŞI (Kurumsal network için)\n");

        // Test raporu başlığı
        report.append("OPENWEATHER API TEST RAPORU\n");
        report.append("Tarih: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))).append("\n");
        report.append("Test Edilen: https://api.openweathermap.org/data/2.5/weather\n");
        report.append("SSL Doğrulama: Devre Dışı (Kurumsal network)\n");
        report.append("=".repeat(60)).append("\n\n");

        try {
            // Tüm testleri çalıştır
            testSuccessfulCityQuery();     // TEST 1
            testInvalidCityQuery();        // TEST 2
            testWithoutApiKey();           // TEST 3
            testResponseFormat();          // TEST 4

            // Raporu yazdır ve kaydet
            printReport();
            saveReportToFile();

        } catch (Exception e) {
            System.err.println("❌ Test sırasında hata: " + e.getMessage());
            report.append("❌ TEST HATASI: ").append(e.getMessage()).append("\n");
            printReport();
        }
    }
    /**
     * TEST 1: Başarılı şehir sorgusu - 200 OK
     */
    public static void testSuccessfulCityQuery() {
        System.out.println("🧪 TEST 1: Başarılı şehir sorgusu (200 OK bekleniyor)");

        try {
            String url = BASE_URL + "?q=Istanbul&appid=" + API_KEY;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            // Assertion
            boolean passed = statusCode == 200 &&
                    response.body().contains("Istanbul") &&
                    response.body().contains("main") &&
                    response.body().contains("temp");

            logTestResult("Başarılı şehir sorgusu",
                    "200 OK ve geçerli yanıt",
                    passed,
                    "Status: " + statusCode + ", Istanbul verisi: " + response.body().contains("Istanbul"));

        } catch (Exception e) {
            logTestResult("Başarılı şehir sorgusu",
                    "200 OK ve geçerli yanıt",
                    false,
                    "HATA: " + e.getMessage());
        }
    }
    /**
     * TEST 2: Hatalı şehir sorgusu - 404 Not Found
     */
    public static void testInvalidCityQuery() {
        System.out.println("🧪 TEST 2: Hatalı şehir sorgusu (404 Not Found bekleniyor)");

        try {
            String url = BASE_URL + "?q=InvalidCityName12345&appid=" + API_KEY;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            // Assertion
            boolean passed = statusCode == 404;

            logTestResult("Hatalı şehir sorgusu",
                    "404 Not Found",
                    passed,
                    "Status: " + statusCode + ", Body: " + (response.body().length() > 100 ? response.body().substring(0, 100) + "..." : response.body()));

        } catch (Exception e) {
            logTestResult("Hatalı şehir sorgusu",
                    "404 Not Found",
                    false,
                    "HATA: " + e.getMessage());
        }
    }

    /**
     * TEST 3: API Key olmadan çağrı - 401 Unauthorized
     */
    public static void testWithoutApiKey() {
        System.out.println("🧪 TEST 3: API Key olmadan çağrı (401 Unauthorized bekleniyor)");

        try {
            String url = BASE_URL + "?q=Istanbul";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            // Assertion
            boolean passed = statusCode == 401;

            logTestResult("API Key olmadan çağrı",
                    "401 Unauthorized",
                    passed,
                    "Status: " + statusCode + ", Body: " + (response.body().length() > 100 ? response.body().substring(0, 100) + "..." : response.body()));

        } catch (Exception e) {
            logTestResult("API Key olmadan çağrı",
                    "401 Unauthorized",
                    false,
                    "HATA: " + e.getMessage());
        }
    }

    /**
     * TEST 4: Yanıt formatı doğrulama
     */
    public static void testResponseFormat() {
        System.out.println("🧪 TEST 4: Yanıt formatı doğrulama");

        try {
            String url = BASE_URL + "?q=London&appid=" + API_KEY;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            String responseBody = response.body();

            // Tüm required field'ları kontrol et
            boolean hasAllFields = responseBody.contains("\"name\"") &&
                    responseBody.contains("\"main\"") &&
                    responseBody.contains("\"temp\"") &&
                    responseBody.contains("\"pressure\"") &&
                    responseBody.contains("\"humidity\"") &&
                    responseBody.contains("\"weather\"") &&
                    responseBody.contains("\"description\"") &&
                    responseBody.contains("\"wind\"") &&
                    responseBody.contains("\"speed\"") &&
                    responseBody.contains("\"sys\"") &&
                    responseBody.contains("\"country\"");

            boolean passed = statusCode == 200 && hasAllFields;

            logTestResult("Yanıt formatı doğrulama",
                    "Tüm required field'lar mevcut",
                    passed,
                    "Status: " + statusCode + ", Tüm field'lar: " + hasAllFields);

        } catch (Exception e) {
            logTestResult("Yanıt formatı doğrulama",
                    "Tüm required field'lar mevcut",
                    false,
                    "HATA: " + e.getMessage());
        }
    }
    /**
     * Test sonuçlarını logla
     */
    private static void logTestResult(String testName, String expected, boolean passed, String details) {
        String result = passed ? "✅ PASS" : "❌ FAIL";
        System.out.println("   " + result + " - " + testName);
        System.out.println("   ↳ " + details + "\n");

        // Rapor için kaydet
        report.append("TEST: ").append(testName).append("\n");
        report.append("BEKLENEN: ").append(expected).append("\n");
        report.append("SONUÇ: ").append(passed ? "PASS" : "FAIL").append("\n");
        report.append("DETAY: ").append(details).append("\n");
        report.append("-".repeat(50)).append("\n");
    }
    /**
     * Raporu konsola yazdır
     */
    private static void printReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎯 FİNAL TEST RAPORU");
        System.out.println("=".repeat(60));
        System.out.println(report.toString());
    }
    /**
     * Raporu dosyaya kaydet
     */
    private static void saveReportToFile() {
        try {
            String fileName = "weather-api-test-raporu-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss")) + ".txt";

            Files.writeString(Path.of(fileName), report.toString(), StandardOpenOption.CREATE);
            System.out.println("📄 Test raporu kaydedildi: " + fileName);
        } catch (Exception e) {
            System.err.println("Rapor dosyası kaydedilemedi: " + e.getMessage());
        }
    }
}