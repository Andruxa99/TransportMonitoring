package sample.GUI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.*;

import java.io.File;
import java.net.MalformedURLException;

public class GUI {
    private static GUI instance;
    private static Stage primaryStage;
    private static Controller controller;

    private static AnchorPane root;

    private GUI(Stage primaryStage, Controller controller){
        this.primaryStage = primaryStage;
        this.controller = controller;
    }

    public static GUI getInstance(Stage primaryStage, Controller controller){
        if (instance == null) instance = new GUI(primaryStage, controller);
        return instance;
    }

    public void createGUI() throws MalformedURLException {
        root = new AnchorPane();
        var scene = new Scene(root, 900,600);
        scene.getStylesheets().add(getClass().getResource("MyStyle.css").toExternalForm());

        /*createImagePanel(System.getProperty("user.dir") + "/src/sample/Resources/Pictures/background.jpg",
                900, 600);*/
        createImagePanel(System.getProperty("user.dir") + "/src/sample/Resources/Pictures/title.png",
                320,450);
        createSearchPanel();

        primaryStage.setTitle("Transport Monitoring");
        primaryStage.setResizable(false);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createImagePanel(String path, double width, double height) throws MalformedURLException {
        var file = new File(path);
        var titlePicture = new Image(file.toURI().toURL().toString(), width, height, false, false);
        root.getChildren().add(new ImageView(titlePicture));
    }

    private void createSearchPanel() throws MalformedURLException {
        var searchPanel = new AnchorPane();
        searchPanel.setId("searchPanel");

        ObservableList<String> types = FXCollections.observableArrayList("Автобус", "Троллейбус");
        var transportSelector = new ComboBox<>(types);
        transportSelector.setValue("Автобус");
        assignAnchor(transportSelector, 275, 30, 30);

        var numLabel = new Label("Номер маршрута:");
        assignAnchor(numLabel, 0, 10.0, 95.0);
        var stNameLabel = new Label("Название остановки:");
        assignAnchor(stNameLabel, 0, 10.0, 160.0);
        var holidayLabel = new Label("Праздничный день");
        assignAnchor(holidayLabel, 0, 10, 225);

        var numList = new ComboBox<String>();
        numList.setPromptText("Номер маршрута");
        numList.setEditable(true);
        controller.setStartNumList(numList);
        assignAnchor(numList, 275.0, 30.0, 95.0);

        var stNameList = new ComboBox<String>();
        stNameList.setPromptText("Остановка");
        stNameList.setEditable(true);
        stNameList.setDisable(true);
        assignAnchor(stNameList, 275.0, 30.0, 160.0);

        var holidayCheck = new CheckBox();
        assignAnchor(holidayCheck, 275.0, 250.0, 220.0);

        var searchButton = new Button("Найти");
        searchButton.setDisable(true);
        searchButton.setId("searchButton");
        assignAnchor(searchButton, 120.0, 120.0, 280.0);

        controller.transportSelectorChange(transportSelector, numList);
        controller.numListTextChange(numList, stNameList, holidayCheck.isSelected());
        controller.stNameListTextChange(stNameList, searchButton);
        controller.searchButtonClick(transportSelector, numList, stNameList, searchButton, holidayCheck, createResultPanel());

        searchPanel.getChildren().addAll(transportSelector,numLabel,numList,stNameLabel,stNameList, holidayLabel,
                holidayCheck,searchButton);

        AnchorPane.setLeftAnchor(searchPanel, 350.0);
        AnchorPane.setRightAnchor(searchPanel,10.0);
        AnchorPane.setTopAnchor(searchPanel,10.0);

        root.getChildren().add(searchPanel);
    }

    private AnchorPane createResultPanel(){
        var resultPanel = new AnchorPane();
        resultPanel.setVisible(false);
        resultPanel.setId("resultPanel");

        AnchorPane.setLeftAnchor(resultPanel, 30.0);
        AnchorPane.setRightAnchor(resultPanel,30.0);
        AnchorPane.setTopAnchor(resultPanel,350.0);

        root.getChildren().add(resultPanel);
        return resultPanel;
    }

    private void assignAnchor(Control control, double left, double right, double top){
        AnchorPane.setLeftAnchor(control, left);
        AnchorPane.setRightAnchor(control,right);
        AnchorPane.setTopAnchor(control,top);
    }
}
