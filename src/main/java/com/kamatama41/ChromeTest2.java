package com.kamatama41;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;

public class ChromeTest2 {

	public static void main(String[] args) throws Exception {
		
		long startTime=System.currentTimeMillis();
		
		ChromeDriverService service = new ChromeDriverService.Builder()
				.usingDriverExecutable(new File("./webdriver/chromedriver")).usingAnyFreePort().build();

		// Driverを初期化
		WebDriver driver = new ChromeDriver(service);

		LinkedHashMap<String, String> testDataMap = new LinkedHashMap<String, String>();
		testDataMap.put("cms_sei_kanji", "本宮");
		testDataMap.put("cms_mei_kanji", "真魚");
		testDataMap.put("cms_sei_kana", "モトミヤ");
		testDataMap.put("cms_mei_kana", "マオ");
		testDataMap.put("cms_tanjou_ymd_nen", "2015");
		testDataMap.put("cms_yuubin_no_1", "981");
		testDataMap.put("cms_yuubin_no_2", "0904");
		testDataMap.put("cms_juusho", "仙台市青葉区青葉町１−１２青葉荘215");
		testDataMap.put("cms_denwa_no_1", "070");
		testDataMap.put("cms_denwa_no_2", "1234");
		testDataMap.put("cms_denwa_no_3", "5678");
		testDataMap.put("cms_mail_address", "choco@google.com");
		testDataMap.put("cms_mail_address_chk", "choco@google.com");
		testDataMap.put("sin_password", "potatpo");
		testDataMap.put("sin_password_chk", "potatpo");

		try {

			// Googleを開く
			driver.get("https://www.jins-jp.com/Customer/Register");

			for (Map.Entry<String, String> e : testDataMap.entrySet()) {
				// 対象のidのelementを探す
				WebElement element = driver.findElement(By.id(e.getKey()));

				// テキストボックスに文字を入力する
				element.sendKeys(e.getValue());

				// テキストボックスをちょっとずらしたところをクリックする
				Actions builder = new Actions(driver);
				builder.moveToElement(element, -10, -0).click().build().perform();

				String DATE_FORMAT = "yyyyMMddHHmmss";

				// スクリーンショットを取る(画面)
				screenShotAsChorome(driver, DATE_FORMAT, e.getKey());

				TimeUnit.SECONDS.sleep(1);

				// スクリーンショットを取る(全体)
				screenShotAsAll(DATE_FORMAT, e.getKey());

			}
		} finally {

			// Driverを閉じる
			driver.quit();
		}
		
		long endTime=System.currentTimeMillis();
		System.out.println("time=" + (endTime - startTime));
		
	}

	// スクリーンショット（画面）
	private static void screenShotAsChorome(WebDriver driver, String dateFormat, String key) {
		WebDriver augmentedDriver = new Augmenter().augment(driver);
		File screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);

		// 取ったファイルをscreenshot/{クエリ}_yyyyMMddHHmmss.jpg として保存
		Path dst = Paths.get(".", "screenshot",
				new SimpleDateFormat(dateFormat).format(new Date()) + "_" + key + "_chorome" + ".jpg");
		try {
			Files.copy(screenshot.toPath(), dst, StandardCopyOption.COPY_ATTRIBUTES);
		} catch (IOException ee) {
			ee.printStackTrace();
		}
	}

	// スクリーンショット（デスクトップ全体）
	private static void screenShotAsAll(String dateFormat, String key) {
		try {
			Robot rbt;
			rbt = new Robot();
			Toolkit tk = Toolkit.getDefaultToolkit();
			Dimension dim = tk.getScreenSize();
			BufferedImage background = rbt
					.createScreenCapture(new Rectangle(0, -1, (int) dim.getWidth(), (int) dim.getHeight()));
			Path dst = Paths.get(".", "screenshot",
					new SimpleDateFormat(dateFormat).format(new Date()) + "_" + key + "_all" + ".jpg");
			ImageIO.write(background, "png", dst.toFile());
		} catch (AWTException | IOException eee) {
			throw new IllegalStateException(eee);
		}
	}

	// ※ブラウザによってheight,width,つなぎ目の貼り付け位置の修正が必要
	// スクリーンショット（画面全体）
	private static void screenShotAsScroll(WebDriver driver, String dateFormat, String key)
			throws WebDriverException, IOException, InterruptedException {

		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		driver.switchTo().defaultContent();
		TakesScreenshot ts = (TakesScreenshot) new Augmenter().augment(driver);

		// JS実行用のExecuter
		JavascriptExecutor jexec = (JavascriptExecutor) driver;

		// 画面サイズで必要なものを取得
		int innerH = Integer.parseInt(String.valueOf(jexec.executeScript("return window.innerHeight")));
		int innerW = Integer.parseInt(String.valueOf(jexec.executeScript("return window.innerWidth")));
		int scrollH = Integer
				.parseInt(String.valueOf(jexec.executeScript("return document.documentElement.scrollHeight")));
		
		// イメージを扱うための準備
		BufferedImage img = new BufferedImage(innerW*2, scrollH*2, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		
		BufferedImage img2 = new BufferedImage(innerW*2, scrollH, BufferedImage.TYPE_INT_ARGB);
		Graphics g2 = img2.getGraphics();


		// スクロールを行うかの判定
		if (innerH > scrollH) {
			BufferedImage imageParts = ImageIO.read(ts.getScreenshotAs(OutputType.FILE));
			g.drawImage(imageParts, 0, 0, null);
		} else {
			int scrollableH = scrollH;
			int i = 0;

			// スクロールしながらなんどもイメージを結合していく
			while (scrollableH > innerH) {
				BufferedImage imageParts = ImageIO.read(ts.getScreenshotAs(OutputType.FILE));
				
				//
				g2.drawImage(imageParts, 0, 0, null);
				
				Path dst = Paths.get(".", "screenshot", new SimpleDateFormat(dateFormat).format(new Date())
						+ "_" + key + "_scroll" + i+ ".jpg");
				ImageIO.write(img2, "png", dst.toFile());
				//
				if(i==0){
					g.drawImage(imageParts, 0, 0, null);
				}else{
					g.drawImage(imageParts, 0, innerH * (i+1), null);
				}
				
				
				scrollableH = scrollableH - innerH;
				i++;
				jexec.executeScript("window.scrollTo(0," + innerH * i + ")");		
				
				TimeUnit.SECONDS.sleep(1);
				
			}

			// 一番下まで行ったときは、下から埋めるように貼り付け
			BufferedImage imageParts = ImageIO.read(ts.getScreenshotAs(OutputType.FILE));
//			g.drawImage(imageParts, 0, scrollH - innerH, null);
			g.drawImage(imageParts, 0, scrollH +415 , null);
		}
		
		Path dst = Paths.get(".", "screenshot", new SimpleDateFormat(dateFormat).format(new Date())
				+ "_" + key + "_scroll" + ".jpg");
		ImageIO.write(img, "png", dst.toFile());
		
	}
	
}
