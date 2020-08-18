package sample.GUI;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import sample.Monitor.OnlineMonitor;
import sample.Monitor.ScheduleMonitor;

import java.io.File;
import java.net.MalformedURLException;

public class Controller {
    private static Controller instance;
    private static ScheduleMonitor scheduleMonitor;
    private static OnlineMonitor onlineMonitor;

    private Controller(ScheduleMonitor scheduleMonitor, OnlineMonitor onlineMonitor){
        this.scheduleMonitor = scheduleMonitor;
        this.onlineMonitor = onlineMonitor;
    }

    public static Controller getInstance(ScheduleMonitor scheduleMonitor, OnlineMonitor onlineMonitor){
        if (instance == null) instance = new Controller(scheduleMonitor, onlineMonitor);
        return instance;
    }

    public void transportSelectorChange(ComboBox<String> selector, ComboBox<String> numList){
        selector.setOnAction(actionEvent -> {
            if (selector.getValue().equals("Автобус"))
                numList.setItems(scheduleMonitor.getNumbers("bus"));
            else numList.setItems(scheduleMonitor.getNumbers("tbus"));
        });
    }

    public void numListTextChange(ComboBox<String> numList, ComboBox<String> stNameList, boolean holidayCheck){
        numList.getEditor().textProperty().addListener((observableValue, s, t1) -> {
            stNameList.getItems().clear();
            var num = numList.getEditor().getText();
            if (num.equals("") || !numList.getItems().contains(num))
                stNameList.setDisable(true);
            else {
                Runnable run = ()-> {
                    stNameList.setItems(scheduleMonitor.getStations(num, holidayCheck));
                };
                var numThread = new Thread(run);
                numThread.start();
                stNameList.setDisable(false);
            }
        });
    }

    public void stNameListTextChange(ComboBox<String> stNameList, Button searchButton){
        stNameList.getEditor().textProperty().addListener((observableValue, s, t1) -> {
            if (stNameList.getEditor().getText().equals("") || !stNameList.getItems().contains(stNameList.getEditor().getText()))
                searchButton.setDisable(true);
            else searchButton.setDisable(false);
        });
    }

    public void searchButtonClick(ComboBox<String> selector, ComboBox<String> numList, ComboBox<String> stNameList, Button searchButton, CheckBox holidayCheck,  AnchorPane resultPanel){
        searchButton.setOnAction(actionEvent -> {
            resultPanel.getChildren().clear();
            resultPanel.setVisible(true);

            var type = selector.getValue().toLowerCase();
            var number = numList.getEditor().getText();
            var station = stNameList.getEditor().getText();
            String nextStation = "";
            if (stNameList.getItems().indexOf(station) != stNameList.getItems().size() - 1)
                nextStation = stNameList.getItems().get(stNameList.getItems().indexOf(station) + 1);


            var near = "";
            try {
                near += onlineMonitor.getTime(number, station, nextStation, selector.getValue());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            var result = "";
            var scTimes = "";
            for (var time : scheduleMonitor.getTimes(station, near, holidayCheck.isSelected()))
                scTimes += time + " ";

            if (!near.equals("null")) {
                if (near.equals(""))
                    result = "Вероятно, транспорт уже не ходит." + "\n\n";
                else result = "Ближайший " + type + " ожидается через " + near + "\n\n";
            }
            else result = "Не удалось найти информацию о ближайшем транспорте." + "\n\n";
            result += "Время прибытия следующего транспорта по расписанию: " + scTimes;
            var info = type + " номер " + number + "\n" +
                    station + " -> " + nextStation + "\n\n";
            PrintResult(resultPanel, info + result);
        });
    }

    private void PrintResult(AnchorPane resultPanel, String result){
        var timesLabel = new Label(result);
        AnchorPane.setLeftAnchor(timesLabel, 100.0);
        AnchorPane.setTopAnchor(timesLabel, 30.0);
        resultPanel.getChildren().add(timesLabel);

        var file = new File(System.getProperty("user.dir") + "/src/sample/Resources/Pictures/border.png");
        Image borderPicture = null;
        try {
            borderPicture = new Image(file.toURI().toURL().toString(), 900, 1000,
                    false, false);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        resultPanel.getChildren().add(new ImageView(borderPicture));
    }

    public void setStartNumList(ComboBox<String> numList){
        numList.setItems(scheduleMonitor.getNumbers("bus"));
    }
}
