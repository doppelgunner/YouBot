package com.doppelgunner.youbot;

import com.doppelgunner.youbot.model.VideoGroup;
import com.doppelgunner.youbot.task.BackgroundTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by robertoguazon on 13/07/2017.
 */
public class Util {

    public static final String PROPERTIES_FILENAME = "youtube.properties";
    public static final String APPLICATION_NAME = "You_Bot";

    //https://stackoverflow.com/questions/24239332/google-oauth-handle-a-revoked-authorization
    //https://developers.google.com/identity/protocols/OAuth2UserAgent#validatetoken
    public static void revokeAccess(String accessToken) {

        try {
            String url = "https://accounts.google.com/o/oauth2/revoke";

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url);

            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("token",accessToken));
            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse response = client.execute(post);
            System.out.println("revokeAccess [Response code]: " + response.getStatusLine().getStatusCode());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static String getYoutubeID(String youtubeURL) {
        int start = youtubeURL.indexOf("v=");
        int amper = youtubeURL.indexOf('&');
        return youtubeURL.substring((start == -1) ? 0 : start + 2, (amper == -1) ? youtubeURL.length() : amper);
    }

    public static YouTube getYouTubeDefault() {
        return new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {}
        }).setApplicationName(APPLICATION_NAME).build();
    }

    public static Auth getAuth() {
        try {
            List<String> scopes = new ArrayList<>();
            scopes.add("https://www.googleapis.com/auth/youtube.force-ssl");
            scopes.add("https://www.googleapis.com/auth/userinfo.profile");
            scopes.add("https://www.googleapis.com/auth/userinfo.email");
            Auth auth = new Auth(scopes, APPLICATION_NAME);
            return auth;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Credential getCredential() {
        return getCredential(getAuth());
    }

    public static Credential getCredential(Auth auth) {
        try {
            return auth.authorize();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static YouTube getYouTubeAuthentication() {
        return getYouTubeAuthentication(getCredential());
    }

    public static YouTube getYouTubeAuthentication(Credential credential) {
        return new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
    }

    public static Properties getProperties() {
        Properties properties = new Properties();
        try {
            InputStream in = Util.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
            properties.load(in);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return properties;
    }

    public static List<SearchResult> searchYouTubeVideos(String query, String order, Long maxResults) {
        return searchYouTubeVideos(getYouTubeDefault(), query, order, maxResults);
    }

    public static List<SearchResult> searchYouTubeVideos(YouTube youtube, String query, String order, Long maxResults) {
        try {
            YouTube.Search.List search = youtube.search().list("id,snippet");

            String apiKey = getProperties().getProperty("youtube.apiKey");
            search.setKey(apiKey);
            search.setQ(query);
            search.setType("video");
            search.setFields("items(id/videoId,snippet/title,snippet/thumbnails/default/url,snippet/channelId,snippet/channelTitle,snippet/description)");
            search.setMaxResults(maxResults);
            search.setOrder((order != null && !order.isEmpty()) ? order : "relevance");

            SearchListResponse searchListResponse = search.execute();
            List<SearchResult> searchResults = searchListResponse.getItems();
            return searchResults;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Use this with {@link #getYouTubeAuthentication()} for higher quota
     * @param query
     * @param maxResults
     * @return
     */
    public static List<Video> getYouTubeVideos(YouTube youtube, String query, String order, Long maxResults) {
        try {
            List<SearchResult> results = searchYouTubeVideos(youtube, query, order, maxResults);
            String videoIds = results
                    .stream()
                    .map(result -> result.getId().getVideoId())
                    .collect(Collectors.joining(","));
            YouTube.Videos.List listVideoRequest = youtube.videos().list("snippet, recordingDetails").setId(videoIds);
            VideoListResponse listResponse = listVideoRequest.execute();

            return listResponse.getItems();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<Video> getYouTubeVideos(String query, String order, Long maxResults) {
        return getYouTubeVideos(getYouTubeAuthentication(), query, order, maxResults);
    }

    /*
    public static Image createJavaFXImage(String path) {
        return new javafx.scene.image.Image("file:" + path);
    }
    */

    public static Oauth2 getOauth2Authentication(Credential credential) {
        try {
            Oauth2 oauth2 = new Oauth2.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            return oauth2;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Userinfoplus getUserProfile(Oauth2 oauth2) {
        try {
            Userinfoplus userinfoplus = oauth2.userinfo().get().execute();
            return userinfoplus;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean tryParseInt(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean tryParseLong(String string) {
        try {
            Long.parseLong(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean range(int b1, int b2, int n) {
        if (n >= b1 && n <= b2) return true;
        if (n >= b2 && n <= b1) return true;
        return false;
    }

    public static boolean range(long b1, long b2, long n) {
        if (n >= b1 && n <= b2) return true;
        if (n >= b2 && n <= b1) return true;
        return false;
    }

    public static void notify(String title, String message, NotificationType type) {
        TrayNotification tray = new TrayNotification();
        tray.setTitle(title);
        tray.setMessage(message);
        tray.setNotificationType(type);
        tray.setAnimationType(AnimationType.POPUP);
        tray.showAndDismiss(Duration.seconds(2));
    }

    /**
     * Need the youtube w/ authentication ({@link #getYouTubeAuthentication()} for commenting on youtube videos. Also,
     *  this uses the scopes needed
     * @param youTubeAuthentication
     * @param videoId
     * @param comment
     */
    public static void comment(YouTube youTubeAuthentication, String videoId, String comment) {
        try {
            CommentSnippet commentSnippet = new CommentSnippet();
            commentSnippet.setTextOriginal(comment);
            Comment topLevelComment = new Comment();
            topLevelComment.setSnippet(commentSnippet);

            CommentThreadSnippet commentThreadSnippet = new CommentThreadSnippet();
            commentThreadSnippet.setVideoId(videoId);
            commentThreadSnippet.setTopLevelComment(topLevelComment);

            CommentThread commentThread = new CommentThread();
            commentThread.setSnippet(commentThreadSnippet);

            youTubeAuthentication.commentThreads().insert("snippet",commentThread).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T>ObservableList<T> removeDuplicates(ObservableList<T> list) {
        return FXCollections.observableArrayList(list.stream().distinct().collect(Collectors.toList()));
    }

    public static void toJSON(Object o, String path) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(path), o);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T>T fromJSON(Class c, String path) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return (T)mapper.readValue(new File(path), c);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void save(Object o, String filename) {
        toJSON(o,"data/save/" + filename);
    }

    public static VideoGroup[] load(String filepath) {
        return fromJSON(VideoGroup[].class, filepath);

    }

    public static void saveList(ObservableList<VideoGroup> list) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save video list");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("YouBot Video List","*.youbot.json")
        );
        fileChooser.setInitialDirectory(new File("data/save"));
        File file = fileChooser.showSaveDialog(YouBot.getPrimaryStage());
        save(list.toArray(),file.getName());
    }

    public static VideoGroup[] chooseList() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose saved video list");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("YouBot Video List","*.youbot.json")
        );
        fileChooser.setInitialDirectory(new File("data/save"));
        File file = fileChooser.showOpenDialog(YouBot.getPrimaryStage());
        if (file != null) {
            return load(file.getPath());
        }
        return null;
    }

    public static void runBackground(Runnable toRun, Runnable onSucceded, boolean daemon) {
        BackgroundTask task = new BackgroundTask(toRun);
        Thread thread = new Thread(task);
        thread.setDaemon(daemon);
        thread.start();
        task.setOnSucceeded(e -> {
            if (onSucceded != null) {
                onSucceded.run();
            }
        });
    }

    public static void saveComment(String comment) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save comment");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("YouBot Comment","*.youbot.comment.json")
        );
        fileChooser.setInitialDirectory(new File("data/save/comment"));
        File file = fileChooser.showSaveDialog(YouBot.getPrimaryStage());
        toJSON(comment,"data/save/comment/" + file.getName());
    }

    public static String chooseComment() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose saved comment");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("YouBot Comment","*.youbot.comment.json")
        );
        fileChooser.setInitialDirectory(new File("data/save/comment"));
        File file = fileChooser.showOpenDialog(YouBot.getPrimaryStage());
        if (file != null) {
            return fromJSON(String.class,file.getPath());
        }
        return null;
    }

    public static ImageView makeImageView(String path) {
        return makeImageView(path,20,20);
    }

    public static ImageView makeImageView(String path, int width, int height) {
        ImageView im = new ImageView(new Image(path));
        im.setFitHeight(width);
        im.setFitWidth(height);
        return im;
    }

    public static void goLink(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
