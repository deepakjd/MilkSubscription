package com.comorinland.deepak.milksubscription.Common;

public class ProductInfo
{
    private String strProductName;
    private String strProductDesc;
    private int    iPrice;
    private String strImageName;


    public ProductInfo(String strProductName, String strProductDesc, int price, String strImageName)
    {
        this.strProductName = strProductName;
        this.strProductDesc = strProductDesc;
        this.iPrice         = price;
        this.strImageName   = strImageName;
    }

    public String getProductName() {
        return strProductName;
    }

    public String getProductDesc() {
        return strProductDesc;
    }

    public int getPrice() {return iPrice;}

    public String getImageName() { return strImageName;}

}
