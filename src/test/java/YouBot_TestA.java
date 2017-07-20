
import com.doppelgunner.youbot.Util;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
/**
 * Created by robertoguazon on 14/07/2017.
 */
public class YouBot_TestA {

    @Test
    public void oauth2UserProfile() {
        Userinfoplus me = Util.getUserProfile(Util.getOauth2Authentication(Util.getCredential(Util.getAuth())));
        System.out.println("plusProfile()");
        System.out.println("Name: " + me.getName());
        System.out.println("\t - email: " + me.getEmail());
        System.out.println("\t - gender: " + me.getGender());
        System.out.println("\t - image url: " + me.getPicture());
    }

    @Test
    public void youtubeID() {
        String youtubeURL = "https://www.youtube.com/watch?v=n1lE7wgBYGs";
        assertEquals(Util.getYoutubeID(youtubeURL),"n1lE7wgBYGs");
        assertEquals(Util.getYoutubeID("n1lE7wgBYGs"), "n1lE7wgBYGs");
    }

    @Test
    public void searchVideos() {
        //test search
        List<SearchResult> searchResults = Util.searchYouTubeVideos("Java javafx", "relevance", 2L);

        System.out.println("searchVideos()");
        int counter = 0;
        for (SearchResult r : searchResults) {
            System.out.println(++counter + " - " + r.getSnippet().getTitle());
            System.out.println("\t - videoID: " + r.getId().getVideoId());
            System.out.println("\t - thumbnail: " + r.getSnippet().getThumbnails().getDefault().getUrl());
            System.out.println("\t - channelID: " + r.getSnippet().getChannelId());
            System.out.println("\t - channel title: " + r.getSnippet().getChannelTitle());
        }

        assertTrue(searchResults.size() > 0);
    }

    @Test
    public void getVideos() {
        List<Video> videos = Util.getYouTubeVideos("Javafx java", "relevance", 2L);
        assertTrue(videos.size() > 0);
    }
}
