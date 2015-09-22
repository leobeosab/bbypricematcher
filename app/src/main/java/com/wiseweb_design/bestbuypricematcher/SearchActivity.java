package com.wiseweb_design.bestbuypricematcher;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SearchViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class SearchActivity extends ActionBarActivity
{
    public SearchHandler search;
    public HashMap<String, String> bbyInf;
    TextView bestBuyView;
    TextView amazonView;
    TextView neweggView;
    TextView priceMatchAvail;
    TextView sellingInformation;
    TextView item;
    boolean priceMatchAvailTrue;
    String lowestSeller;
    double lowestPrice;
    int sku;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        bestBuyView = (TextView) findViewById(R.id.best_buy_view);
        amazonView = (TextView) findViewById(R.id.amazon_view);
        neweggView = (TextView) findViewById(R.id.newegg_view);
        priceMatchAvail = (TextView) findViewById(R.id.price_match_avail);
        sellingInformation = (TextView) findViewById(R.id.lowest_seller);
        item = (TextView) findViewById(R.id.item);
        lowestSeller = "BestBuy";
        lowestPrice = 0.0;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        final MenuItem search = menu.findItem(R.id.sku_search);
        if (search != null)
        {
            SearchView searchView = (SearchView) search.getActionView();
            if (searchView != null)
            {
                SearchViewCompat.setInputType(searchView, InputType.TYPE_CLASS_NUMBER);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()

                {
                    @Override
                    public boolean onQueryTextSubmit(String query)
                    {
                        MenuItem searchMenuItem = search;
                        if (searchMenuItem != null)
                        {
                            if (query.length() == 7)
                            {
                                sku = Integer.parseInt(query);
                                new searchSku().execute();
                                searchMenuItem.collapseActionView();
                            }
                            else
                                Toast.makeText(getBaseContext(), "Incorrect Format SKU", Toast.LENGTH_SHORT).show();
                            //call search function here
                        }
                        return false;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText)
                    {
                        // ...
                        return true;
                    }
                });
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();


        switch (item.getItemId())
        {
            case R.id.sku_search:
                //SearchView search = (SearchView) findViewById(R.id.sku_search);

//                openSearch()
                return true;

        }

        return super.onOptionsItemSelected(item);
    }
    public void setSellerInfo(String seller, String price)
    {
        sellingInformation.setText("Seller: " + (lowestSeller = seller) + " Price: $" + (lowestPrice = Double.parseDouble(price.replace("$", "").replace(",",""))));
    }
    // Title AsyncTask
    private class searchSku extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            search = new SearchHandler(sku);
            bbyInf = search.getBestBuyInformation();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!bbyInf.get("GoodSKU").equals("false"))
            {
                // Set title into TextView
                if (bbyInf.get("title").length() > 35)
                    item.setText(bbyInf.get("title").substring(0, 35) + "...");
                else
                    item.setText(bbyInf.get("title"));

                bestBuyView.setText("Available: Yes  Price: " + bbyInf.get("price"));
                setSellerInfo("BestBuy", bbyInf.get("price"));
                new searchAmazon().execute();
            }
            else
                Toast.makeText(getBaseContext(), "Invalid SKU", Toast.LENGTH_SHORT).show();
        }
    }
    private class searchAmazon extends AsyncTask<Void, Void, Void>
    {
        ArrayList<String> amazon;
        @Override
        protected void onPreExecute()
        {

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            if (bbyInf != null)
            {
                amazon = search.searchAmazon(search.jSoupDoc, bbyInf.get("upc"), bbyInf);
            }
        else
            {
                amazon = new ArrayList<>();
                amazon.add("What");
                Toast.makeText(getBaseContext(), "Error, amazon search called before best buy", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Set title into TextView
            if (amazon.size() >= 1)
            {
                amazonView.setText("Available: Yes  Price: "+amazon.get(0));
                double amazonPrice;
                if ( (amazonPrice = Double.parseDouble(amazon.get(0).replace("$","").replace(",",""))) < lowestPrice)
                {
                    setSellerInfo("Amazon.com", amazon.get(0));
                    if (!priceMatchAvailTrue)
                    {
                        priceMatchAvail.setText("Price Match Available: YES");
                        priceMatchAvailTrue = true;
                    }
                }
            }
            else
            {
                amazonView.setText("Available: Not available  Price: N/A");
            }
            Toast.makeText(getBaseContext(), "Amazon.com, done", Toast.LENGTH_SHORT).show();
            new searchNewEgg().execute();
        }
    }

    private class searchNewEgg extends AsyncTask<Void, Void, Void>
    {
        String newegg;
        @Override
        protected void onPreExecute()
        {

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            if (bbyInf != null)
            {
                newegg = search.searchWallmart(search.jSoupDoc, bbyInf.get("upc"), bbyInf);
            }
            else
            {
                newegg = "Error";
                Toast.makeText(getBaseContext(), "Error, amazon search called before best buy", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Set title into TextView
            if (!newegg.equals(""))
            {
                neweggView.setText("Available: Yes  Price: "+newegg);
                double neweggPrice;
                if ( (neweggPrice = Double.parseDouble(newegg.replace("$","").replace(",",""))) < lowestPrice)
                {
                    setSellerInfo("Newegg.com", newegg);
                    if (!priceMatchAvailTrue)
                    {
                        priceMatchAvail.setText("Price Match Available: YES");
                        priceMatchAvailTrue = true;
                    }
                }
            }
            else
            {
                neweggView.setText("Available: Not available  Price: N/A");
            }
            Toast.makeText(getBaseContext(), "Newegg.com, done", Toast.LENGTH_LONG).show();
        }
      }

}
