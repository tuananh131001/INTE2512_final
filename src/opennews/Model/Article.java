/*
  RMIT University Vietnam
  Course: INTE2512 Object-Oriented Programming
  Semester: 2021B
  Assessment: Final Project
  Created  date: 09/06/2021
  Author:
  Nguyen Tuan Anh s3864077
  Tran Nguyen Ha Khanh s3877707
  Nguyen Vu Minh Duy s3878076
  Phan Thanh Phu s3877814
  Ngo Thanh Nguyen s3856221
  Last modified date: 14/09/2021
  Acknowledgement:
  http://www.java2s.com/Tutorials/Java/JavaFX_How_to/Image/Load_an_Image_from_local_file_system.htm
  https://docs.oracle.com/javafx/2/webview/jfxpub-webview.htm
  https://stackoverflow.com/questions/6530974/getting-a-property-value-and-passing-it-on-to-superclass/6531076#6531076
  https://stackoverflow.com/questions/47743650/javafx-8-property-bindings-for-custom-objects
  https://stackoverflow.com/questions/21083945/how-to-avoid-not-on-fx-application-thread-currentthread-javafx-application-th
  https://stackoverflow.com/questions/541487/implements-runnable-vs-extends-thread-in-java?page=2&tab=votes#tab-top
  https://stackoverflow.com/questions/4691533/java-wait-for-thread-to-finish
  https://stackoverflow.com/questions/48048943/javafx-8-scroll-bar-css
*/
package opennews.Model;

import javafx.scene.image.Image;

import java.time.Duration;

public class Article {
    protected Image image;
    protected String titleArticle;
    protected String sourceArticle;
    protected Duration timeArticle;
    protected String source;

    public Article(Image image, String titleArticle, String sourceArticle, Duration timeArticle, String source) {
        this.image = image;
        this.titleArticle = titleArticle;
        this.sourceArticle = sourceArticle;
        this.timeArticle = timeArticle;
        this.source = source;
    }

    public String getTitleArticle() {
        return titleArticle;
    }

    public String getSourceArticle() {
        return sourceArticle;
    }


    public Duration getTimeArticle(){ return timeArticle; }

    public String getSource() { return source; }

    public Image getImageArticle() { return image; }
}
