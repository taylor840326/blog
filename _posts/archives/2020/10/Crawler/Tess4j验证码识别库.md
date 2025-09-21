Java 验证码识别库 Tess4j 教程 (含代码和工程)
BY BRIEFCOPY · 2017年11月27日

在用Java开发爬虫时，一个重要的问题就是如何破解网站的验证码。这里介绍一个Java验证码识别库Tess4j，可以破解一些简单的验证码。

Tess4j完整Eclipse示例工程下载
可加QQ群426491390（专知-人工智能交流）在群文件中下载完整DEMO工程（包含所需Jar包、语言数据和测试图片）。

Tess4j配置
配置Tess4j主要包括两个步骤：

导入Tess4j的Jar包或用Maven导入Tess4j的依赖，Tess4j官方没有直接提供Jar包下载，可加QQ群419678039在群文件中下载完整DEMO工程（包含所需Jar包和数据）。
导入识别相关语言的数据，数据下载地址：https://github.com/tesseract-ocr/tessdata（上述DEMO工程中已包含英文语言包，可识别英文和数字）
Tess4j识别验证码示例代码

import java.io.File;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * 本教程由 http://bbs.datahref.com/ 提供
 * Tess4j验证码识别示例
 * 工程中tessdata文件夹包含了识别英文所需的数据
 * 需要识别其他语言课到https://github.com/tesseract-ocr/tessdata下载相关数据
 * 放到tessdata文件夹中
 * @author hu
 *
 */
public class OCRDemo {

    public static void main(String[] args) throws TesseractException {
        ITesseract instance = new Tesseract();
        File imgDir = new File("img_data");
        //对img_data文件夹中的每个验证码进行识别
        //文件名即正确的结果
        for (File imgFile : imgDir.listFiles()) {
            //该例子输入的是文件，也可输入BufferedImage           
            String ocrResult = instance.doOCR(imgFile);
            //输出图片文件名，即正确识别结果
            System.out.println("ImgFile: "+imgFile.getAbsolutePath());
            //输出识别结果
            System.out.println("OCR Result: " + ocrResult);
        }   
    }
}
测试图片：

d2fa5347303f3b0e7ce6860f30c7c34b (1)

识别结果：

ImgFile: D:\workplace\Tess4jDemo\img_data\hKRPu.png
OCR Result: hKRPu


ImgFile: D:\workplace\Tess4jDemo\img_data\HyUbb.png
OCR Result: HyUbb


ImgFile: D:\workplace\Tess4jDemo\img_data\iCnG3.png
OCR Result: i CnGS


ImgFile: D:\workplace\Tess4jDemo\img_data\otRhg.png
OCR Result: otRhg


ImgFile: D:\workplace\Tess4jDemo\img_data\UACZp.png
OCR Result: UACZp