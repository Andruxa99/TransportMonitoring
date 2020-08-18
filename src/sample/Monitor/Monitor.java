package sample.Monitor;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

public abstract class Monitor implements AutoCloseable{
    protected WebDriver driver;
    protected static Logger LOGGER;
    static {
        try(FileInputStream ins = new FileInputStream(System.getProperty("user.dir") + "/src/sample/Resources/Log/log.config")){
            LogManager
                    .getLogManager()
                    .readConfiguration(ins);
            LOGGER = Logger.getLogger(Monitor.class.getName());
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
    }

    protected Monitor(String address){
        System.setProperty("webdriver.gecko.driver", System.getProperty("user.dir") + "/geckodriver.exe");
        var options = new FirefoxOptions();
        options.addArguments("--headless");

        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(800, TimeUnit.MILLISECONDS);
        try {
            LOGGER.info("Open page " + address);
            driver.get(address);
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING,"The page "  +  address + " couldn't be opened", e);
        }
    }

    @Override
    public void close(){
        try {
            LOGGER.info("Close page");
            driver.close();
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "The page couldn't be closed", e);
        }
    }
}
