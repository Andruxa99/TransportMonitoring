package sample.Main;

import javafx.application.Application;
import javafx.stage.Stage;
import sample.GUI.Controller;
import sample.GUI.GUI;
import sample.Monitor.OnlineMonitor;
import sample.Monitor.ScheduleMonitor;

import java.net.MalformedURLException;

public class Main extends Application {
    static ScheduleMonitor scheduleMonitor;
    static OnlineMonitor onlineMonitor;

    @Override
    public void start(Stage primaryStage) throws InterruptedException, MalformedURLException {
        Runnable scRun = ()-> {
            scheduleMonitor = ScheduleMonitor.getInstance("https://cdsvyatka.com/kirov/schedule");
        };
        var scheduleThread = new Thread(scRun);
        scheduleThread.start();

        Runnable onRun = ()->{
            onlineMonitor = OnlineMonitor.getInstance("https://cdsvyatka.com/kirov/map");
        };
        var onlineThread = new Thread(onRun);
        onlineThread.start();

        scheduleThread.join();
        onlineThread.join();

        var controller = Controller.getInstance(scheduleMonitor, onlineMonitor);
        var mainGUI = GUI.getInstance(primaryStage, controller);
        mainGUI.createGUI();
    }

    @Override
    public void stop(){
        scheduleMonitor.close();
        onlineMonitor.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

