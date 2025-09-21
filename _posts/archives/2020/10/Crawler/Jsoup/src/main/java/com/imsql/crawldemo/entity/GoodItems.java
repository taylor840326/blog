package com.imsql.crawldemo.entity;


public class GoodItems {
    private String title;
    private String image;
    private String price;

    public GoodItems() {
    }

    public GoodItems(String title, String image, String price) {
        this.title = title;
        this.image = image;
        this.price = price;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "GoodItems{" +
                "title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
