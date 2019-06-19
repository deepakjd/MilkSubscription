package com.comorinland.deepak.milksubscription.Common;

import android.content.Context;

import java.io.File;

/**
 * Created by deepak on 23/8/18. We will have this one file which has Internal file storage related handling.
 */

public class InternalFileStorageUtils
{
    private Context mContext;
    private String mStrStorageDirectory = "MilkManPhotos/";

    public InternalFileStorageUtils(Context context)
    {
        mContext = context;
    }

    public String GetStorageDirectory()
    {
        String strStorageDirectory = mContext.getFilesDir() + File.separator + mStrStorageDirectory;
        return  strStorageDirectory;
    }

    public boolean AreMilkVarietyPhotosAvailable(String strZipFileName)
    {
        String strZipDirectoryName;

        strZipDirectoryName = GetDirectoryFromStorageKey(strZipFileName);
        if (strZipDirectoryName.isEmpty())
        {
            return Boolean.FALSE;
        }
        File dir = new File(mContext.getFilesDir(), mStrStorageDirectory + strZipDirectoryName);
        if (dir.exists() && dir.isDirectory())
        {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private String GetDirectoryFromStorageKey(String strZipFileName)
    {
        if ((strZipFileName != null) && (strZipFileName.isEmpty() == Boolean.FALSE))
        {
            if (strZipFileName.contains("."))
            {
                String parts[] = strZipFileName.split("[.]");
                //To get the file name portion before the .zip"
                String strZipNameWithoutExt = parts[0];
                return strZipNameWithoutExt + File.separator;
            }
        }
        return "";
    }

    public String GetFullPathFromImageName(String strZipFileName,String strImageName)
    {
       String strFullPathName = mContext.getFilesDir() + File.separator + mStrStorageDirectory + GetDirectoryFromStorageKey(strZipFileName) + strImageName;
       return strFullPathName;
    }

}
