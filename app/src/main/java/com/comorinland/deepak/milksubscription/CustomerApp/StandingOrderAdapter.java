package com.comorinland.deepak.milksubscription.CustomerApp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.comorinland.deepak.milksubscription.Common.InternalFileStorageUtils;
import com.comorinland.deepak.milksubscription.Common.ProductInfo;
import com.comorinland.deepak.milksubscription.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class StandingOrderAdapter extends RecyclerView.Adapter<StandingOrderAdapter.StandingOrderHolder>
{
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<ProductInfo> arrListProductInfo;
    private InternalFileStorageUtils mInternalFileStorageUtils;
    private String mStrZipFileName;

    public StandingOrderAdapter(Context context, ArrayList<ProductInfo> values)
    {
        this.mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        arrListProductInfo = new ArrayList<ProductInfo>();
        arrListProductInfo.addAll(values);
    }


    public static class StandingOrderHolder extends RecyclerView.ViewHolder
    {
        TextView txtProductTitle, txtProductDesc;
        ImageView imgProduct;

        public StandingOrderHolder(View viewItem)
        {
            super(viewItem);

            txtProductTitle = (TextView)viewItem.findViewById(R.id.txtProductName);
            txtProductDesc = (TextView)viewItem.findViewById(R.id.txtProductDesc);
            imgProduct = (ImageView)viewItem.findViewById(R.id.imgProduct);

        }

    }

    //Must override, this inflates our Layout and instantiates and assigns
    //it to the ViewHolder.
    @Override
    public StandingOrderHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View viewItem;

        viewItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.standing_order_list_info, parent, false);

        return new StandingOrderHolder(viewItem);
    }

    @Override
    public void onBindViewHolder(StandingOrderHolder holder, int position)
    {

        ProductInfo productInfo = arrListProductInfo.get(position);

        holder.txtProductTitle.setText(productInfo.getProductName());
        holder.txtProductDesc.setText(productInfo.getProductDesc());

        String strProductImageName = productInfo.getImageName();

        String strFullPathToImageFile = mInternalFileStorageUtils.GetFullPathFromImageName(mStrZipFileName,strProductImageName);

        File fileImage = new File(strFullPathToImageFile);

        if (fileImage.exists())
            Picasso.get().load(fileImage).fit().centerCrop().into(holder.imgProduct);
    }

    @Override
    public int getItemCount()
    {
        return arrListProductInfo.size();
    }

}