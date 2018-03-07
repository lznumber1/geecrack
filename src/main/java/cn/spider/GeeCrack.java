package cn.spider;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

/**
 * 破解点融网注册极验验证码 https://www.dianrong.com/account/create
 * 
 * @author zhe.li
 *
 */
public class GeeCrack {

	private static final String CHROME_DRIVER_PATH = "D:/dev/driver/chromedriver.exe";

	public void crack() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(df.format(new Date()) + ">>>>开始点融网滑块验证");

		String tel1 = "18252886520";
		String tel2 = "18641258998";

		boolean success = false;
		int exeCount = 0;
		while (!success && ++exeCount < 10) {
			exeCount++;
			try {
				String tel = null;
				if (exeCount % 2 == 0) {
					tel = tel1;
				} else {
					tel = tel2;
				}
				success = reg(tel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println(df.format(new Date()) + ">>>>结束点融网滑块验证");
	}

	private boolean reg(String tel) throws Exception {
		WebDriver driver = null;
		try {
			driver = chrome();
			driver.get("https://www.dianrong.com/account/create");
			driver.findElement(By.id("account-phone")).sendKeys(tel);
			driver.findElement(By.cssSelector("div.accept-agreement > div.row > div.col-xs-1 > label")).click();
			driver.findElement(By.cssSelector("div.button-next > button")).click();
			TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000) + 4000);// 等待页面加载完毕

			System.out.println("secondStepForm===>>>"
					+ driver.findElement(By.cssSelector("form[name=secondStepForm]")).isDisplayed());

			WebElement snap = driver.findElement(By.cssSelector("div.dr-geetest > div.gt_holder > svg"));
			BufferedImage beIm = taskScreenShot(driver, snap, null);
			// BufferedImage beIm = taskScreenShot(driver, snap, new
			// File("d:/img/gee/bg_be.png"));

			WebElement dragElement = driver.findElement(
					By.cssSelector("div.dr-geetest > div.gt_holder > svg > g[transform] > g:nth-child(2) > circle"));

			Actions actions = new Actions(driver);
			actions.clickAndHold(dragElement).perform();
			TimeUnit.MILLISECONDS.sleep(500);

			BufferedImage afIm = taskScreenShot(driver, snap, null);
			// BufferedImage afIm = taskScreenShot(driver, snap, new
			// File("d:/img/gee/bg_af.png"));

			int distince = calDistince(beIm, afIm);
			move(driver, dragElement, distince, actions);

			// System.out.println("secondStepForm===>>>"
			// +
			// driver.findElement(By.cssSelector("form[name=secondStepForm]")).isDisplayed());

			String text = driver.getPageSource();
			if (text.contains("手机号已注册")) {
				System.out.println("手机号已注册");
				return true;
			} else if (driver.findElement(By.cssSelector("form[name=secondStepForm]")).isDisplayed()) {
				if (text.contains("验证码已发送")) {
					System.out.println("手机号未注册");
					return false;
				} else {
					System.out.println("未知异常");
					// throw new Exception("未知异常");
					return false;
				}
			} else {
				System.out.println("滑块验证失败");
				// throw new Exception("滑块验证失败");
				return false;
			}
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}

	}

	private BufferedImage taskScreenShot(WebDriver driver, WebElement element, File saveFile) {
		File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		try {
			BufferedImage im = ImageIO.read(src);
			BufferedImage subim = im.getSubimage(element.getLocation().getX(), element.getLocation().getY(),
					element.getSize().getWidth(), element.getSize().getHeight() - 56);
			if (saveFile != null) {
				ImageIO.write(subim, "png", saveFile);
			}
			return subim;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void move(WebDriver driver, WebElement element, int distince, Actions actions) throws Exception {
		System.out.println("应平移距离：" + distince);
		if (distince < 1) {
			return;
		}
		Random r = new Random();
		int d = 0;
		while (d < distince - 2) {
			int x = r.nextInt(3) + 1;
			d += x;
			actions.moveByOffset(x, 0).perform();
			Thread.sleep(new Random().nextInt(7));
		}
		actions.release(element).perform();
		TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000) + 4000);// 等待页面加载完毕
	}

	private int calDistince(BufferedImage im1, BufferedImage im2) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < im1.getWidth(); x++) {
			int num = 0;
			for (int y = 0; y < im1.getHeight() - 25; y++) {
				Color c1 = new Color(im1.getRGB(x, y));
				Color c2 = new Color(im2.getRGB(x, y));
				int r = Math.abs(c2.getRed() - c1.getRed());
				int g = Math.abs(c2.getGreen() - c1.getGreen());
				int b = Math.abs(c2.getBlue() - c1.getBlue());
				if (r + g + b > 100) {
					num += 1;
				}
			}
			if (num > 10) {
				sb.append(1);
			} else {
				sb.append(0);
			}
		}

		String s = sb.toString();
		// System.out.println(s);

		int len = s.length();
		int start = 0;
		int end = 0;
		int i = s.indexOf("1");
		while (i < len && i != -1) {
			int j = s.indexOf("0", i + 1);
			if (j == -1) {
				break;
			}
			if (j - i > 15) {
				if (start == 0) {
					start = i;
				} else {
					end = i;
					break;
				}
			}
			i = s.indexOf("1", j);
		}
		return end - start;
	}

	private WebDriver chrome() {

		System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, CHROME_DRIVER_PATH);

		ChromeOptions chromeOptions = new ChromeOptions();
		// 以headless模式打开chrome
		// chromeOptions.addArguments("--headless");
		// chromeOptions.addArguments("--disable-gpu");
		// chromeOptions.addArguments("--no-sandbox");
		chromeOptions.addArguments("--window-size=1366,768");
		// chromeOptions.addArguments("--remote-debugging-port=9222");

		WebDriver driver = new ChromeDriver(chromeOptions);

		driver.manage().window().setSize(new Dimension(1366, 768));
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS).pageLoadTimeout(60, TimeUnit.SECONDS)
				.setScriptTimeout(30, TimeUnit.SECONDS);

		return driver;
	}

	public static void main(String[] args) throws Exception {
		GeeCrack geeCrack = new GeeCrack();
		geeCrack.crack();
	}

}
