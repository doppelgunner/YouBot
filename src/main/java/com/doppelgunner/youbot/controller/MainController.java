package com.doppelgunner.youbot.controller;

import com.doppelgunner.youbot.Order;
import com.doppelgunner.youbot.Util;
import com.doppelgunner.youbot.YouBot;
import com.doppelgunner.youbot.model.VideoGroup;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.sun.xml.internal.ws.client.sei.ResponseBuilder;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tray.notification.NotificationType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by robertoguazon on 15/07/2017.
 */
public class MainController extends Controller {

    @FXML private ScrollPane scrollPane;
    @FXML private HeaderController headerController;

    @FXML private TextField searchTextField;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;
    @FXML private ComboBox<String> orderyByComboBox;
    @FXML private ListView<VideoGroup> resultsListView;
    @FXML private ListView<VideoGroup> selectedListView;
    @FXML private TextArea commentTextArea;
    @FXML private Button sendCommentButton;
    @FXML private TextField maxSearchTextField;
    @FXML private Button addAllResultsButton;
    @FXML private Button clearAllSelectedButton;
    @FXML private Button addResultsButton;
    @FXML private Button clearSelectedButton;
    @FXML private Button saveSelectedButton;
    @FXML private Button loadSelectedButton;
    @FXML private Label selectedCountLabel;
    @FXML private Label resultsCountLabel;
    @FXML private VBox commentVBox;
    @FXML private Button saveCommentButton;
    @FXML private Button loadCommentButton;

    private Long currentMaxSearch = 10L;
    private ObservableList<VideoGroup> resultsList;
    private ObservableList<VideoGroup> selectedList;

    private void disableApp(boolean b) {
        scrollPane.setDisable(b);
    }

    private void disableComment(boolean b) {
        commentVBox.setDisable(b);
    }

    public HeaderController getHeaderController() {
        return headerController;
    }

    @FXML
    protected void initialize() {
        super.initialize();

        scrollPane.setFitToWidth(true);
        commentTextArea.setWrapText(true);

        //disableApp(true);
        //YouBot.getLoggedInProperty().addListener((o,ov,nv) -> disableApp(!nv));
        disableComment(true);
        YouBot.getLoggedInProperty().addListener((o,ov,nv) -> disableComment(!nv));

        ObservableList<String> orders = FXCollections.observableArrayList();
        for (Order order : Order.all) {
            orders.add(order.toString());
        }
        orderyByComboBox.setItems(orders);
        orderyByComboBox.getSelectionModel().select(Order.RELEVANCE.toString());

        int min = 1;
        int max = 50;
        maxSearchTextField.focusedProperty().addListener((o,ov,nv) -> {
            String text = "" + currentMaxSearch;

            if (nv) {
                maxSearchTextField.clear();
            } else {
                String newText = maxSearchTextField.getText();
                if (Util.tryParseLong(newText)) {
                    Long parsed = Long.parseLong(newText);
                    if (Util.range(min,max,parsed)) {
                        text = "" + parsed;
                        currentMaxSearch = parsed;
                    }
                }
                maxSearchTextField.setText(text);
            }
        });

        resultsList = FXCollections.observableArrayList();
        selectedList = FXCollections.observableArrayList();
        resultsListView.setCellFactory(param -> {
            ListCell<VideoGroup> cell = new ListCell<VideoGroup>() {
                @Override
                protected void updateItem(VideoGroup item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty) {
                        HBox hBox = (HBox)item.getLayoutNode();
                        hBox.setPrefWidth(resultsListView.getPrefWidth() - 30);
                        setGraphic(hBox);
                    } else {
                        setGraphic(null);
                    }
                }
            };
            cell.setOnMouseClicked(me -> {
                if (cell.isEmpty()) me.consume();
            });
            addContextMenu(cell);
            return cell;
        });
        selectedListView.setCellFactory(param -> {
            ListCell<VideoGroup> cell = new ListCell<VideoGroup>() {
                @Override
                protected void updateItem(VideoGroup item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty) {
                        HBox hBox = (HBox)item.getLayoutNode();
                        hBox.setPrefWidth(resultsListView.getPrefWidth() - 30);
                        setGraphic(hBox);
                    } else {
                        setGraphic(null);
                    }
                }
            };
            cell.setOnMouseClicked(me -> {
                if (cell.isEmpty()) me.consume();
            });
            addContextMenu(cell);
            return cell;
        });

        resultsListView.setItems(resultsList);
        selectedListView.setItems(selectedList);

        resultsListView.setOnMouseClicked(me -> {
            if (me.getButton().equals(MouseButton.PRIMARY)) {
                if (me.getClickCount() == 2) {
                    VideoGroup vg = resultsListView.getSelectionModel().getSelectedItem();
                    if (vg != null) {
                        vg.openInBrowser();
                    }
                }
            }
        });

        selectedListView.setOnMouseClicked(me -> {
            if (me.getButton().equals(MouseButton.PRIMARY)) {
                if (me.getClickCount() == 2) {
                    VideoGroup vg = selectedListView.getSelectionModel().getSelectedItem();
                    if (vg != null) {
                        vg.openInBrowser();
                    }
                }
            }
        });

        ListProperty resultLP = new SimpleListProperty<>(resultsList);
        resultLP.addListener((o,ov,nv) ->
            resultsCountLabel.setText("(" + resultsList.size() + ")"));

        ListProperty selectedLP = new SimpleListProperty<>(selectedList);
        selectedLP.addListener((o,ov,nv) ->
            selectedCountLabel.setText("(" + selectedList.size() + ")"));

        searchTextField.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.ENTER) {
                search();
            }
        });
        setIcons();
    }

    private void addContextMenu(ListCell<VideoGroup> cell) {

        ContextMenu cm = new ContextMenu();
        MenuItem openInBrowserItem = new MenuItem("Open video in browser");
        MenuItem copyTitleItem = new MenuItem("Copy title to clipboard");
        MenuItem copyUploaderItem = new MenuItem("Copy uploader to clipboard");
        MenuItem copyVideoURLItem = new MenuItem("Copy video URL to clipboard");
        MenuItem copyVideoIDItem = new MenuItem("Copy video ID to clipboard");
        cm.getItems().addAll(
                openInBrowserItem,
                copyTitleItem,
                copyUploaderItem,
                copyVideoURLItem,
                copyVideoIDItem);

        openInBrowserItem.setOnAction(event -> cell.getItem().openInBrowser());
        copyTitleItem.setOnAction(event -> setClipboardContent(cell.getItem().getTitle()));
        copyUploaderItem.setOnAction(event -> setClipboardContent(cell.getItem().getChannelTitle()));
        copyVideoURLItem.setOnAction(event -> setClipboardContent(cell.getItem().getVideoURL()));
        copyVideoIDItem.setOnAction(event -> setClipboardContent(cell.getItem().getVideoId()));

        cell.emptyProperty().addListener((o,wasEmpty,isEmpty) -> {
            if (isEmpty) {
                cell.setContextMenu(null);
            } else {
                cell.setContextMenu(cm);
            }
        });
    }

    private void setClipboardContent(String strContent) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        clipboard.clear();
        ClipboardContent content = new ClipboardContent();
        content.putString(strContent);
        clipboard.setContent(content);
    }

    private void setIcons() {
        ImageView add = Util.makeImageView("images/add.png");
        ImageView addAll = Util.makeImageView("images/add-all.png");
        ImageView clear = Util.makeImageView("images/clear.png");
        ImageView minus = Util.makeImageView("images/minus.png");

        ImageView saveComment = Util.makeImageView("images/save.png");
        ImageView loadComment = Util.makeImageView("images/load.png");
        ImageView saveSelected = Util.makeImageView("images/save.png");
        ImageView loadSelected = Util.makeImageView("images/load.png");

        ImageView search = Util.makeImageView("images/search.png",16,16);
        ImageView clearSearch = Util.makeImageView("images/clear-search.png",14,14);

        ImageView send = Util.makeImageView("images/send.png");

        addResultsButton.setGraphic(add);
        addAllResultsButton.setGraphic(addAll);
        clearSelectedButton.setGraphic(minus);
        clearAllSelectedButton.setGraphic(clear);

        loadCommentButton.setGraphic(loadComment);
        loadSelectedButton.setGraphic(loadSelected);
        saveCommentButton.setGraphic(saveComment);
        saveSelectedButton.setGraphic(saveSelected);

        searchButton.setGraphic(search);
        clearSearchButton.setGraphic(clearSearch);
        sendCommentButton.setGraphic(send);
    }

    @FXML
    private void search() {
        String queryString = searchTextField.getText();
        if (queryString == null || queryString.isEmpty()) {
            Util.notify("YouBot", "Search: Fill up the text field", NotificationType.NOTICE);
            return;
        }
        String orderString = orderyByComboBox.getSelectionModel().getSelectedItem();

        Order chosenOrder = Order.RELEVANCE;
        for (Order order : Order.all) {
            if (order.sameName(orderString)) {
                chosenOrder = order;
                break;
            }
        }

        final Order orderFinal = chosenOrder;
        List<SearchResult> results = new ArrayList<>();
        searchButton.setDisable(true);
        Util.runBackground(
                () -> results.addAll(Util.searchYouTubeVideos(queryString, orderFinal.value(), currentMaxSearch)),
                () -> {
                    resultsList.clear();
                    resultsList.addAll(results
                            .stream()
                            .map(r -> new VideoGroup(r))
                            .collect(Collectors.toList()));
                    searchButton.setDisable(false);
                },
                () -> {
                    searchButton.setDisable(false);
                    Util.notify("YouBot", "Search: Failed no internet connection", NotificationType.NOTICE);
                },
                true
        );
    }

    private void resetChekBoxes(ObservableList<VideoGroup> list) {
        for (VideoGroup vg : list) {
            vg.getCheckBox().setSelected(false);
        }
    }

    private void resetCheckBox(VideoGroup vg) {
        vg.getCheckBox().setSelected(false);
    }

    @FXML
    private void addAllResults() {
        resetChekBoxes(resultsList);
        selectedList.addAll(resultsList);
        resultsList.clear();
    }

    @FXML private void clearSelected() {
        for (int i = 0; i < selectedList.size(); i++) {
            VideoGroup vg = selectedList.get(i);
            if (vg.getCheckBox().isSelected()) {
                selectedList.remove(vg);
                i--;
            }
        }
    }

    @FXML private void addResults() {
        for (int i = 0; i < resultsList.size(); i++) {
            VideoGroup vg = resultsList.get(i);
            if (vg.getCheckBox().isSelected()) {
                resetCheckBox(vg);
                resultsList.remove(vg);
                selectedList.add(vg);
                i--;
            }
        }
    }

    @FXML
    private void clearAllSelected() {
        selectedList.clear();
    }

    @FXML
    private void clearSearch() {
        searchTextField.clear();
    }

    @FXML
    private void saveSelected() {
        if (selectedList.size() <=0) {
            Util.notify("YouBot","Nothing to save", NotificationType.NOTICE);
            return;
        }
        Util.saveList(selectedList);
    }

    @FXML
    private void loadSelected() {
        VideoGroup[] vgs = Util.chooseList();
        if (vgs != null && vgs.length != 0) {
            selectedList.clear();
            selectedList.addAll(vgs);
        }
    }

    @FXML
    private void sendComment() {
        String comment = commentTextArea.getText();
        if (comment == null || comment.isEmpty()) {
            Util.notify("YouBot", "Can't send an empty comment", NotificationType.NOTICE);
            return;
        }
        if (selectedList.isEmpty()) {
            Util.notify("YouBot", "Choose videos to comment on", NotificationType.NOTICE);
            return;
        }
        sendCommentButton.setDisable(true);
        Util.runBackground(
                () -> {
                    for (int i = 0; i < selectedList.size(); i++) {
                        Util.comment(YouBot.getYoutubeAuth(),selectedList.get(i).getVideoId(),comment);
                    }
                },
                () -> {
                    sendCommentButton.setDisable(false);
                },
                () -> {
                    sendCommentButton.setDisable(false);
                    Util.notify("YouBot", "Sending comment failed, check internet connection.", NotificationType.NOTICE);
                },
                false
        );
    }

    @FXML
    private void saveComment() {
        String comment = commentTextArea.getText();
        if (comment == null || comment.isEmpty()) {
            Util.notify("YouBot", "Can't save an empty comment", NotificationType.NOTICE);
            return;
        }
        Util.saveComment(comment);
    }

    @FXML
    private void loadComment() {
        String comment = Util.chooseComment();
        if (comment != null && !comment.isEmpty()) {
            commentTextArea.setText(comment);
        }
    }
}
