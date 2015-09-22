package com.wiseweb_design.bestbuypricematcher;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 * Created by Ryan Wise on 9/3/2015.
 */
public class SearchHandler
{
    int sku = 0000000;
    Document jSoupDoc = null;
    HashMap<String, String> bestBuyInformation;

    public SearchHandler(int sku)
    {
        this.sku = sku;
        init();
    }

    public void init()
    {
        String url = "http://www.bestbuy.com/site/searchpage.jsp?st=" + sku + "&_dyncharset=UTF-8&id=pcat17071&type=page&sc=Global&cp=1&nrp=15&sp=&qp=&list=n&iht=y&usc=All+Categories&ks=960&keys=keys";
        bestBuyInformation = initialBestBuyScan(jSoupDoc, url);
    }

    public HashMap<String, String> initialBestBuyScan(Document doc, String url)
    {
        doc = jsoupConnect(url);
        HashMap<String, String> matchingItems = new HashMap<String, String>();
        matchingItems.put("price", doc.select(".medium-item-price").text());
        matchingItems.put("modelNumber", doc.select(".list-item-info .sku-model ul .model-number").text());
        matchingItems.put("title", doc.select(".list-item-info .sku-title h4 a").text());



        String newURL = "http://bestbuy.com" + bestBuySpecsFormatter(doc.select(".list-item-info .sku-title h4 a").attr("href"));
        System.out.println(newURL);
        doc = jsoupConnect(newURL);
        Elements tableEles = doc.select("#full-specifications table tbody tr");
        for (Element ele : tableEles)
        {
            if (ele.text().contains("UPC"))
            {
                matchingItems.put("upc", ele.text().replace("UPC ", ""));
                break;
            }
        }
        if (tableEles.size() < 1)
            matchingItems.put("GoodSKU", "false");
        else
            matchingItems.put("GoodSKU", "true");
        doc.empty();

        return matchingItems;
    }

    // TODO this obviously, dumbass
    public ArrayList<String> searchNewEgg(Document doc, String url)
    {
        doc = jsoupConnect(url);
        Elements items = doc.select(".s-item-container");
        ArrayList<String> matchingList = new ArrayList<String>();
        for (Element ele : items)
        {

        }
        return new ArrayList<String>();
    }

    public ArrayList<String> searchAmazon(Document doc, String searchTerm, HashMap<String, String> bestBuyInfo)
    {
        String url = "http://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords="+searchTerm;
        doc = jsoupConnect(url);
        Elements items = doc.select(".s-item-container");
        ArrayList<String> matchingList = new ArrayList<String>();
        for (Element ele : items)
        {
            // grab name and price
            String name = ele.select(".a-link-normal").attr("title");
            String price = (ele.select("div div div div div div a.a-link-normal span.a-color-price").text().split(" ")[0]);
            double amazonPrice = 0.0;
            System.out.println(name + "\n" + price);
            if (price.equals(""))
            {
                price = ele.select("span.a-size-base").text().split(" ")[0];
            } else if (price.contains("$"))
            {
                amazonPrice = Double.parseDouble(price.replace("$", "").replace(",", ""));
            }
            if (price.contains("$") && amazonReady(ele.select(".a-link-normal").attr("href"), true))
            {
                matchingList.add(price);
                System.out.println("yes");
            }
            else
                System.out.println("no");
        }
        return matchingList;
    }

    public String searchNewEgg(Document doc, String searchTerm, HashMap<String, String> bestBuyInfo)
    {
        String url = "http://www.newegg.com/Product/ProductList.aspx?Submit=ENE&N=8000%204814%20%20-1&IsNodeId=1&Description="+searchTerm+"&bop=And";
        doc = jsoupConnect(url);
        Elements items = doc.select(".itemCell");
        String finalPrice = "";
        double priceDouble = 999999999;
        for (Element ele : items)
        {
            String price;
            System.out.println(price = (ele.select(".itemAction input").attr("value")));
            if (price != "")
            {
                double temp = cleanDouble(price);
                if (temp < priceDouble)
                {
                    priceDouble = temp;
                }
            }
        }
        if (priceDouble != 999999999)
            finalPrice = "$"+priceDouble;
        return finalPrice;

    }

    public double cleanDouble(String doubleString)
    {
        return Double.parseDouble(doubleString.replace("$","").replace(",",""));
    }

    // TODO To Come: TigerDirect, possibly others
    public String[] searchTigerDirect()
    {
        String[] matchingItems = {};
        return matchingItems;
    }

    // Helper classes
    public Document jsoupConnect(String url)
    {
        try
        {
            return Jsoup.connect(url).timeout(10 * 1000).userAgent("Chrome").get();
        } catch (Exception e)
        {
            System.out.println("Error");
        }
        return new Document(" Failed ");
    }

    public String bestBuySpecsFormatter(String orgURL)
    {
        return orgURL.split("\\?")[0] + ";template=_specificationsTab";
    }

    public boolean amazonReady(String url, boolean havePrice)
    {
        Document tempDoc = jsoupConnect(url);
        boolean goodOption;
        // String price = tempDoc.select("#priceblock_ourprice").text();
        if (havePrice)
        {
            goodOption = (tempDoc.select("#merchant-info").text().toLowerCase().contains("sold by amazon.com"));
        } else
        {
            return false;
        }
        tempDoc.empty();
        return goodOption;
    }

    public boolean priceHelper(double bby, double competetor)
    {
        // TODO improve this
        if (competetor > bby * .66 && competetor < bby * 1.33)
        {
            return true;
        }
        return false;
    }

    public boolean titleMatch(String[] titleFromBBY, String[] titleFromComp)
    {
        int wordCount = 0;
        for (String bbyWord : titleFromBBY)
        {
            for (String compWord : titleFromComp)
            {
                if (bbyWord.toLowerCase().equals(compWord.toLowerCase()))
                    wordCount++;
            }
        }
        if (wordCount >= (titleFromComp.length * .5) || wordCount >= (titleFromBBY.length * .5))
            return true;
        else
            return false;
    }

    public String[] trim(String toBeStripped, String splitter, boolean deleteSpaces, boolean possibleMeasurements)
    {
        toBeStripped = toBeStripped.replace(" - ", " ");
        toBeStripped = toBeStripped.replace(", ", " ");
        toBeStripped = toBeStripped.replace("(", "");
        toBeStripped = toBeStripped.replace(")", "");
        toBeStripped = toBeStripped.replace(" / ", " ");
        if (deleteSpaces)
            toBeStripped = toBeStripped.replace(" ", "");
        if (possibleMeasurements)
            toBeStripped = measurementFormatter(toBeStripped);
        // TODO fix this horrible 7 lines of code

        return toBeStripped.split(splitter);
    }

    public String measurementFormatter(String original)
    {
        return original.replace("\"", "").replace("-Inch", "").replace("'", "").replace("-Feet", "");
    }

    public double priceFormatter(String price)
    {
        return Double.parseDouble(price.replace("$", "").replace(",", ""));
    }

    public HashMap<String, String> getBestBuyInformation()
    {
        return bestBuyInformation;
    }
}
