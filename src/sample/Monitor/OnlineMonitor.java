package sample.Monitor;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.util.logging.Level;

public class OnlineMonitor extends Monitor{
    private static OnlineMonitor instance;

    private OnlineMonitor(String address){
        super(address);
    }

    public static OnlineMonitor getInstance(String address){
        if (instance == null) instance = new OnlineMonitor(address);
        return instance;
    }

    public String getTime(String busNumber, String station, String nextStation, String type) throws InterruptedException {
        station = station.trim();
        nextStation = nextStation.trim();
        try {
            LOGGER.info("Set a new station field in the online monitor");
            var stationBox = driver.findElement(By.xpath("//input[@placeholder=\"Поиск остановки\"]"));
            stationBox.sendKeys(Keys.CONTROL + "a");
            stationBox.sendKeys(Keys.DELETE);
            stationBox.sendKeys(station);
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "Couldn't set a new station field in the online monitor", e);
        }

        var flag = false;
        Thread.sleep(100);
        try {
            flag = false;
            LOGGER.info("Find the right station");
            var listStation = driver.findElements(By.xpath("//div[@class=\"stop-item_to\"]"));
            for (var element : listStation)
                if (element.getText().contains(nextStation) || element.getText().contains(nextStation.toLowerCase())) {
                    element.click();
                    flag = true;
                    break;
                }
            if (!flag) {
                for (int i = 0; i < listStation.size(); i++) {
                    listStation.get(i).click();
                    Thread.sleep(100);
                    for (var stop : driver.findElements(By.xpath("//div[@class=\"prediction_next-stop\"]"))) {
                        if (stop.findElement(By.tagName("span")).getText().equals(nextStation)) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        driver.findElement(By.xpath("//button[@class=\"back-btn\"]")).click();
                        driver.findElement(By.xpath("//input[@placeholder=\"Поиск остановки\"]")).sendKeys(station);
                        listStation = driver.findElements(By.xpath("//div[@class=\"stop-item_to\"]"));
                        Thread.sleep(100);
                    } else break;
                }
            }
            if (!flag) return "null";
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "Couldn't find the right station", e);
        }

        String res = "";
        Thread.sleep(100);
        try {
            LOGGER.info("Find the right time");
            var listOnStation = driver.findElements(By.xpath("//div[@class=\"prediction_route-name-container\"]"));
            if (!listOnStation.isEmpty()) {
                for (var element : listOnStation) {
                    var number = element.findElements(By.tagName("span")).get(0).getText();
                    if (number.equals(busNumber + " " + type)) {
                        res += element.findElements(By.tagName("span")).get(1).getText();
                        break;
                    }
                }
            }
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "Couldn't find the right time", e);
        }

        driver.findElement(By.xpath("//button[@class=\"back-btn\"]")).click();
        return res;
    }
}
