package com.doppelgunner.youbot.model;

import com.doppelgunner.youbot.Util;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.Serializable;

/**
 * Created by robertoguazon on 16/07/2017.
 */
public class VideoGroup {

    private String videoId;

    @JsonIgnore private CheckBox checkBox;
    @JsonIgnore private ImageView thumbnailView;
    @JsonIgnore private Label titleLabel;
    @JsonIgnore private Label descriptionLabel;
    @JsonIgnore private Label channelTitleLabel;

    private String imageURL;
    private String title;
    private String description;
    private String channelTitle;
    private boolean commentsDisabled;

    @JsonIgnore private HBox layoutNode;
    @JsonIgnore private VBox descNode;

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        VideoGroup vg = (VideoGroup) o;
        return this.videoId.equals(vg.getVideoId());
    }

    @Override
    public int hashCode() {
        return videoId.hashCode();
    }

    public VideoGroup() {
        init();
        makeLayout();
    }

    private void init() {
        titleLabel = new Label();
        thumbnailView = new ImageView();
        descriptionLabel = new Label();
        checkBox = new CheckBox();
        layoutNode = new HBox();
        channelTitleLabel = new Label();
        descNode = new VBox();

        thumbnailView.setFitWidth(213);
        thumbnailView.setFitWidth(120);

        titleLabel.setWrapText(true);
        channelTitleLabel.setWrapText(true);
        descriptionLabel.setWrapText(true);

        layoutNode.setPadding(new Insets(5,10,5,10));
        layoutNode.setSpacing(20);
        layoutNode.setAlignment(Pos.CENTER_LEFT);
        descNode.setSpacing(5);

        commentsDisabled = true;
    }

    public VideoGroup(Video video) {
        init();

        setTitle(video.getSnippet().getTitle());
        setDescription(video.getSnippet().getDescription());
        setImageURL(video.getSnippet().getThumbnails().getDefault().getUrl());
        setChannelTitle(video.getSnippet().getChannelTitle());
        setVideoId(video.getId());

        makeLayout();
    }

    public VideoGroup(SearchResult searchResult) {
        init();

        setTitle(searchResult.getSnippet().getTitle());
        setDescription(searchResult.getSnippet().getDescription());
        setImageURL(searchResult.getSnippet().getThumbnails().getDefault().getUrl());
        setChannelTitle(searchResult.getSnippet().getChannelTitle());
        setVideoId(searchResult.getId().getVideoId());
        makeLayout();
    }

    private void makeLayout() {
        //Layout Node(HBox)
        //-thumbnail (Image)
        //  -description(VBox)
        //      -title (Label)
        //      -channel title(Label)
        //      -description (Label)

        descNode.getChildren().addAll(titleLabel,channelTitleLabel,descriptionLabel);

        layoutNode.getChildren().addAll(checkBox,thumbnailView,descNode);
    }

    public void setImageURL(String imageURL) {
        thumbnailView.setImage(new Image(imageURL));
        this.imageURL = imageURL;
    }

    public void setTitle(String title) {
        titleLabel.setText("TITLE: " + title);
        this.title = title;
    }

    public void setDescription(String desc) {
        descriptionLabel.setText("DESCRIPTION: " + desc);
        this.description = desc;
    }

    public void setChannelTitle(String channelTitle) {
        channelTitleLabel.setText("UPLOADED BY: " + channelTitle);
        this.channelTitle = channelTitle;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    @JsonIgnore
    public CheckBox getCheckBox() {
        return checkBox;
    }

    @JsonIgnore
    public Node getLayoutNode() {
        return layoutNode;
    }

    @JsonIgnore
    public void setDescriptionLabel(Label descriptionLabel) {
        this.descriptionLabel = descriptionLabel;
    }

    @JsonIgnore
    public void openInBrowser() {
        Util.goLink(getVideoURL());
    }

    @JsonIgnore
    public String getVideoURL() {
        return Util.getYoutubeWatch() + getVideoId();
    }
}
