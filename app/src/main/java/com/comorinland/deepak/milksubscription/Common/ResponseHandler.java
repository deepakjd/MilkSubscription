package com.comorinland.deepak.milksubscription.Common;

/**
 * Created by deepak on 21/2/18.
 */
public interface ResponseHandler
{
    String HandleJsonResponse(String strResponse);
    void UpdateMilkInfoDisplay(String strReturnCode);
}

