package sample.Monitor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import java.util.*;
import java.util.logging.Level;

public class ScheduleMonitor extends Monitor {
    private static ScheduleMonitor instance;

    private ScheduleMonitor(String address) { super(address); }

    public static ScheduleMonitor getInstance(String address){
        if (instance == null) instance = new ScheduleMonitor(address);
        return instance;
    }

    public ObservableList<String> getNumbers(String type){
        var pattern = (type.equals("bus")) ? "//label[@for=\"transportTypeBus\"]"
                : "//label[@for=\"transportTypeTrolleybus\"]";
        try {
            LOGGER.info("Determine the type of transport in the schedule");
            driver.findElement(By.xpath(pattern)).click();
        }
        catch (Exception e) {
            LOGGER.log(Level.WARNING, "The type of transport in the schedule couldn't be determined", e);
        }
        try{
            ObservableList<String> numbers = FXCollections.observableArrayList();
            LOGGER.info("Get a list of transport numbers in the schedule");
            for (var element : driver.findElements(By.xpath("//select[@title=\"Номер маршрута\"]/*")))
                numbers.add(element.getText());
            return numbers;
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING,"Couldn't get a list of transport numbers in the schedule", e);
            return null;
        }
    }

    public ObservableList<String> getStations(String number, boolean holidayCheck){
        try {
            LOGGER.info("Select the transport number on a schedule page");
            new Select(driver.findElement(By.xpath("//select[@title=\"Номер маршрута\"]"))).selectByVisibleText(number);
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "Couldn't select the transport number on a schedule page", e);
        }
        getToday(holidayCheck);

        try {
            ObservableList<String> stations = FXCollections.observableArrayList();
            LOGGER.info("Get a list of stations in the schedule");
            for (var element : driver.findElements(By.xpath("//select[@title=\"Остановка\"]/*")))
                if (!stations.contains(element.getText()))stations.add(element.getText());
                else stations.add(element.getText() + " ");
            return stations;
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "Couldn't get a list of stations in the schedule", e);
            return null;
        }
    }

    public LinkedList<String> getTimes(String station, String near, boolean holidayCheck){
        getToday(holidayCheck);
        try {
            LOGGER.info("Select the station on a schedule page\"");
            new Select(driver.findElement(By.xpath("//select[@title=\"Остановка\"]"))).selectByVisibleText(station.trim());
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "Couldn't select the station on a schedule page", e);
        }

        var schedule = getSchedule();
        var index = 0;
        if (near.equals("null")) index = getNear(schedule, "0 мин");
        else if (!near.equals("")) index = getNear(schedule, near);
        if (index == schedule.size() - 1) index = 0;
        var times = new LinkedList<String>();
        for (int i = index == 0 || index == schedule.size() - 1 ? 0 : index + 1; i < index + 3 && i < schedule.size(); i++)
            times.add(schedule.get(i).getKey()+":"+schedule.get(i).getValue());
        return times;
    }

    private LinkedList<Pair<String, String>> getSchedule(){
        var schedule = new LinkedList<Pair<String, String>>();
        try {
            LOGGER.info("Get the schedule");
            var table = driver.findElement(By.tagName("table"));
            for (var row : table.findElements(By.tagName("tr")))
                for (var column : row.findElements(By.tagName("td")))
                    schedule.add(new Pair<>(column.getText().substring(0, 2), column.getText().substring(3, 5)));
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "Couldn't get the schedule", e);
        }
        return schedule;
    }

    private int getNear(LinkedList<Pair<String, String>> schedule, String near){
        var curHour = calendar.get(Calendar.HOUR_OF_DAY);
        var curMin = calendar.get(Calendar.MINUTE);

        var nearTime = near.substring(0, near.indexOf(' '));
        var nearHour = Integer.parseInt(nearTime) / 60;
        var nearMin = Integer.parseInt(nearTime) % 60;

        int size = schedule.size();
        boolean flag = false;
        int index = 0;
        for (int i = 0; i < size; i++){
            if (Integer.parseInt(schedule.get(i).getKey()) >= curHour + nearHour) {
                flag = true;
                if (Integer.parseInt(schedule.get(i).getValue()) >= curMin + nearMin) {
                    index = i; break;
                }
            }
            if (Integer.parseInt(schedule.get(i).getKey()) > curHour && flag) {
                index = i; break;
            }
        }
        return index;
    }

    Calendar calendar = Calendar.getInstance();
    private void getToday(boolean holidayCheck){
        calendar.setTime(new Date());
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        List<String> namesDayOfWeek = new LinkedList<>();
        if (dayOfWeek == 1 || holidayCheck) {
            namesDayOfWeek.add("вс.");
            namesDayOfWeek.add("воскресенье");
        }
        else if (dayOfWeek == 4) {
            namesDayOfWeek.add("ср.");
            namesDayOfWeek.add("среда");
        }
        else if (dayOfWeek == 6) {
            namesDayOfWeek.add("пт.");
            namesDayOfWeek.add("пятница");
        }
        else if (dayOfWeek == 7) {
            namesDayOfWeek.add("сб.");
            namesDayOfWeek.add("суббота");
        }
        else namesDayOfWeek.add("пн.");

        try {
            LOGGER.info("Select today's date");
            var dayDropdown = new Select(driver.findElement(By.xpath("//select[@title=\"Дата\"]")));
            var days = dayDropdown.getOptions();
            boolean flag = false;
            for (int i = 0; i < days.size(); i++) {
                for (var nameDay : namesDayOfWeek) {
                    if (days.get(i).getText().contains(nameDay)) {
                        dayDropdown.selectByIndex(i);
                        flag = true;
                        break;
                    }
                }
                if (flag) break;
            }
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "Couldn't select today's date", e);
        }
    }
}

