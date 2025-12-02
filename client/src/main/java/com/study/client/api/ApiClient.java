package com.study.client.api;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.net.HttpCookie;


public class ApiClient {

    private final HttpClient http;
    private final CookieManager cookieManager;
    private final String base;

    public ApiClient(String baseUrl) {
        this.cookieManager = new CookieManager();
        this.cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        this.http = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        this.base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public HttpResponse<String> postJson(String path, String json) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        var res = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("COOKIES AFTER POST = " + cookieManager.getCookieStore().getCookies());
        return res;
    }
    public HttpResponse<String> uploadFile(String path, File file, String title, Long groupId) throws Exception {
        String boundary = "----JavaClientBoundary" + System.currentTimeMillis();

        var fileBytes = Files.readAllBytes(file.toPath());
        var output = new ByteArrayOutputStream();
        var writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);

        // groupId
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"groupId\"\r\n\r\n");
        writer.append(String.valueOf(groupId)).append("\r\n");

        // title
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"title\"\r\n\r\n");
        writer.append(title).append("\r\n");

        // file
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(file.getName()).append("\"\r\n");
        writer.append("Content-Type: application/octet-stream\r\n\r\n");
        writer.flush();

        output.write(fileBytes);
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));

        writer.append("--").append(boundary).append("--").append("\r\n");
        writer.flush();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(base + path))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(output.toByteArray()))
                .build();

        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }


    public void downloadFile(Long resourceId) throws Exception {

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base + "/api/resources/download/" + resourceId))
                .GET()
                .build();

        // Receive REAL BYTES, not a String!
        HttpResponse<byte[]> res =
                http.send(req, HttpResponse.BodyHandlers.ofByteArray());

        if (res.statusCode() != 200) {
            System.out.println("Download failed: " + res.statusCode());
            return;
        }

        String disposition = res.headers()
                .firstValue("Content-Disposition")
                .orElse(null);

        String filename = "resource_" + resourceId;

        if (disposition != null && disposition.contains("filename=")) {
            filename = disposition.substring(disposition.indexOf("filename=") + 9)
                    .replace("\"", "")
                    .trim();
        }

        Path downloadsDir = Paths.get(System.getProperty("user.home"), "Downloads");
        if (!Files.exists(downloadsDir)) {
            Files.createDirectories(downloadsDir);
        }

        Path dest = downloadsDir.resolve(filename);

        // Save the bytes directly
        Files.write(dest, res.body());

        System.out.println("File saved to: " + dest);
    }




    public HttpResponse<String> get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base + path))
                .GET()
                .build();

        var res = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("COOKIES AFTER GET = " + cookieManager.getCookieStore().getCookies());
        return res;
    }

    public HttpResponse<String> delete(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base + path))
                .DELETE()
                .build();

        var res = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("COOKIES AFTER DELETE = " + cookieManager.getCookieStore().getCookies());
        return res;
    }

    public HttpResponse<String> putJson(String path, String json) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        var res = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("COOKIES AFTER PUT = " + cookieManager.getCookieStore().getCookies());
        return res;
    }
    public String getSessionCookie() {
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if ("JSESSIONID".equals(cookie.getName())) {
                return "JSESSIONID=" + cookie.getValue();
            }
        }
        return null;
    }
}
