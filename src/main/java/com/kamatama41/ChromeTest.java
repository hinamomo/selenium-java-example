package com.kamatama41;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class ChromeTest {

    public static void main(String[] args) {
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File("./webdriver/chromedriver"))
                .usingAnyFreePort()
                .build();

        for(final String q : Arrays.asList("リラックマ", "コリラックマ", "キイロイトリ")) {
            // Driverを初期化
            WebDriver driver = new ChromeDriver(service);

            // Googleを開く
            driver.get("https://google.com");

            // "q"というnameのelementを探す(検索ボックスを探す)
            WebElement element = driver.findElement(By.name("q"));

            // 検索ボックスに文字を入力する
            element.sendKeys(q);

            // Submitボタンを押す
            element.submit();

            // 現在のページタイトルをチェック
            System.out.println("タイトル: " + driver.getTitle());

            // 読み込みが終わるまで待つ
            // タイトルが想定したものかどうかをチェック
            (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver d) {
                    return d.getTitle().startsWith(q);
                }
            });

            // 現在のページタイトルをチェック
            System.out.println("タイトル: " + driver.getTitle());

            // スクリーンショットを取る
            WebDriver augmentedDriver = new Augmenter().augment(driver);
            File screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);

            // 取ったファイルをscreenshot/{クエリ}_yyyyMMddHHmmss.jpg として保存
            String DATE_FORMAT = "yyyyMMddHHmmss";
            Path dst = Paths.get(".", "screenshot", q + "_" + new SimpleDateFormat(DATE_FORMAT).format(new Date()) + ".jpg");
            try {
                Files.copy(screenshot.toPath(), dst, StandardCopyOption.COPY_ATTRIBUTES);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Driverを閉じる
            driver.quit();
        }
    }
}
