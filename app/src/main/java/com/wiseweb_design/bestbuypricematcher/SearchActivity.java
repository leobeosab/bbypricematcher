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
    int sku;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        bestBuyView = (TextView) findViewById(R.id.best_buy_view);
        amazonView = (TextView) findViewById(R.id.amazon_view);
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
                            sku = Integer.parseInt(query);
                            new searchSku().execute();
                            searchMenuItem.collapseActionView();
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
    public void searchSku(int sku)
    {

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
            // Set title into TextView
            bestBuyView.setText("Name: "+bbyInf.get("title") + "\nModel Number" + bbyInf.get("modelNumber") + "\nPrice: " + bbyInf.get("price"));
            new searchAmazon().execute();
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
                Toast.makeText(getBaseContext(), "Error, amazon search called before best buy", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Set title into TextView
            if (amazon.size() >= 1)
            {
                amazonView.setText("Price Match Available: Yes\nPrice: "+amazon.get(0));
            }
            else
            {
                amazonView.setText("Price Match Available: No\nPrice: Product not sold on Amazon");
            }
            Toast.makeText(getBaseContext(), "Amazon, done", Toast.LENGTH_LONG).show();
        }
    }

}
