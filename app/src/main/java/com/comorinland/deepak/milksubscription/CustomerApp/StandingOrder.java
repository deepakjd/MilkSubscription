package com.comorinland.deepak.milksubscription.CustomerApp;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.comorinland.deepak.milksubscription.Common.Constant;
import com.comorinland.deepak.milksubscription.Common.DownloadFromAmazonDBTask;
import com.comorinland.deepak.milksubscription.Common.ProductInfo;
import com.comorinland.deepak.milksubscription.Common.ResponseHandler;
import com.comorinland.deepak.milksubscription.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class StandingOrder extends Fragment implements ResponseHandler
{
    private StandingOrderViewModel mViewModel;
    private ProgressDialog mProgressDialog;
    private ArrayList<ProductInfo> mArrayProductInfo;

    public static StandingOrder newInstance()
    {
        return new StandingOrder();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.standing_order_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(StandingOrderViewModel.class);

        // TODO: Use the ViewModel

        new DownloadFromAmazonDBTask(StandingOrder.this, "GetDefaultDelivery.php", mProgressDialog);
    }

    /* This function is going to get the string response and store it in a string array */
    @Override
    public String HandleJsonResponse(String strResponse)
    {
        if (strResponse == null)
        {
            return Constant.RESPONSE_UNAVAILABLE;
        }

        if (strResponse.equals("QUERY_EMPTY"))
        {
            return Constant.INFO_NOT_FOUND;
        }

        if (strResponse.equals("DB_EXCEPTION"))
        {
            return Constant.DB_ERROR;
        }

        try
        {
            JSONArray responseArray = new JSONArray(strResponse);

            for (int i = 0; i < responseArray.length(); i++)
            {

                JSONObject dataObject = responseArray.getJSONObject(i);

                String strProductName = dataObject.getString("Product Name");
                String strProductDesc = dataObject.getString("Product Description");
                String strImageName = dataObject.getString("Image Name");
                int    iPrice = dataObject.getInt("Price");

                ProductInfo p = new ProductInfo(strProductName,strProductDesc,iPrice,strImageName);
                mArrayProductInfo.add(p);
            }
        }
        catch (JSONException e)
        {
            return Constant.JSON_EXCEPTION;
        }

        return Constant.JSON_SUCCESS;

    }

    public void UpdateMilkInfoDisplay(String strReturnCode)
    {
        if (strReturnCode.equals(Constant.JSON_SUCCESS))
        {
            StandingOrderAdapter adapter;

            RecyclerView DisplayRecyclerView = (RecyclerView) getView().findViewById(R.id.standing_order_recycler_view);

            adapter = new StandingOrderAdapter(getContext(), mArrayProductInfo);

            DisplayRecyclerView.setAdapter(adapter);

            DisplayRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        }
    }
}
