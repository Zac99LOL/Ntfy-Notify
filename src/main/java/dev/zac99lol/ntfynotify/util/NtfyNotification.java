package dev.zac99lol.ntfynotify.util;

import dev.zac99lol.ntfynotify.NtfyNotifyConfig;
import net.minecraft.client.Minecraft;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class NtfyNotification {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private String message;
    private String title;
    private Priority priority;
    private List<String> tags;

    public NtfyNotification(String message) {
        this.message = message;
    }

    public NtfyNotification(String message, String title) {
        this.message = message;
        this.title = title.isEmpty() ? null : title;
    }

    public NtfyNotification(String message, String title, Priority priority) {
        this.message = message;
        this.title = title.isEmpty() ? null : title;
        this.priority = priority;
    }

    public NtfyNotification(String message, String title, List<String> tags) {
        this.message = message;
        this.title = title.isEmpty() ? null : title;
        this.tags = tags.isEmpty() ? null : tags;
    }

    public NtfyNotification(String message, String title, Priority priority, List<String> tags) {
        this.message = message;
        this.title = title.isEmpty() ? null : title;
        this.priority = priority;
        this.tags = tags.isEmpty() ? null : tags;
    }

    private void send(String topic, URI server) {
        HttpRequest request = toHttpRequest(URI.create(server + topic));

        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }

    public void send(String topic) {
        send(topic, URI.create(NtfyNotifyConfig.INSTANCE.server));
    }

    public void send() {
        send(NtfyNotifyConfig.INSTANCE.topic, URI.create(NtfyNotifyConfig.INSTANCE.server));
    }

    public HttpRequest toHttpRequest(URI uri) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.POST(HttpRequest.BodyPublishers.ofString(message));
        if (title != null) builder.header("X-Title", title);
        if (priority != null) builder.header("X-Priority", String.valueOf(priority.toInt()));
        if (tags != null) builder.header("X-Tags", String.join(",", tags));
        builder.uri(uri);

        return builder.build();
    }

    public enum Priority {
        MINIMUM,
        LOW,
        DEFAULT,
        HIGH,
        MAX;

        public int toInt() {
            return this.ordinal() + 1;
        }
    }
}
