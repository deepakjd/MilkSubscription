package com.comorinland.deepak.milksubscription.Common;

import android.content.Context;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by deepak on 18/8/18.
 */

/*
 We get the Pre-signed URL as a parameter. We use this URL to get the zip file from the S3 bucket.
 We then Unzip this zip file and store the individual files in internal storage.
*/

public class DownloadFromS3Task extends AsyncTask<String,Void,String>
{
    private Context mContext;
    private static final int BUFFER_SIZE = 1024;
    private File mFileZipFile;
    private InternalFileStorageUtils mInternalFileStorageUtils;

    public DownloadFromS3Task(Context context)
    {
        mContext = context;
    }

    @Override
    protected void onPreExecute()
    {

    }

    /* This is the function that is going to download the zip file and store it in
       a location.
     */

    @Override
    protected String doInBackground(String... params)
    {
        String strSignInURL;
        String strZipFileName;

        HttpURLConnection urlConnection = null;

        int count = 0;

        /* We need to use the Sign-in URL to download the zip file */
        strSignInURL = params[0];
        strZipFileName = params[1];

        mInternalFileStorageUtils = new InternalFileStorageUtils(mContext);
        String strParentDirectory = mInternalFileStorageUtils.GetStorageDirectory();

        File fileStorageDirectory = new File(strParentDirectory);

        if (!fileStorageDirectory.exists())
        {
            fileStorageDirectory.mkdir();
        }

         mFileZipFile = new File(strParentDirectory + strZipFileName);

        try
        {
            URL url = new URL(strSignInURL);

            urlConnection = (HttpURLConnection) url.openConnection();

            FileOutputStream outputStream = new FileOutputStream(mFileZipFile);

            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null)
            {
                // Nothing to do
                return null;
            }

            byte data[] = new byte[BUFFER_SIZE];

            while ((count = inputStream.read(data)) != -1)
            {
                outputStream.write(data, 0, count);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            // Close Stream and disconnect HTTP connection.
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
        }

        return "SUCCESS";
    }

    /* This is the function that will extract a single individual file from the
       bigger zip file.
     */

    private void extractFile(ZipInputStream zipIn, String strFullFilePath )
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];

        int count;

        try
        {
            FileOutputStream fos = new FileOutputStream(new File(strFullFilePath));

            // reading and writing
            while ((count = zipIn.read(buffer)) != -1)
            {
                baos.write(buffer, 0, count);
                byte[] bytes = baos.toByteArray();
                fos.write(bytes);
                baos.reset();
            }
            fos.flush();
            fos.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Extracts a Zip file
     */

    private void extractZipFile() throws IOException
    {
        ZipInputStream zipInputStream;

        String strFileAbsolutePath = mInternalFileStorageUtils.GetStorageDirectory();

        FileInputStream fos = new FileInputStream(mFileZipFile);

        zipInputStream = new ZipInputStream(fos);

        ZipEntry zipEntry = zipInputStream.getNextEntry();

        while (zipEntry != null)
        {
            if (!zipEntry.isDirectory())
            {
                // if the entry is a file, extracts it
                extractFile(zipInputStream, strFileAbsolutePath + zipEntry.getName());
            }
            else
            {
                // if the entry is a directory, make the directory
                File dir = new File(strFileAbsolutePath +zipEntry.getName());
                dir.mkdir();
            }
            zipInputStream.closeEntry();
            zipEntry = zipInputStream.getNextEntry(); // Get the next entry.
        }
    }

    /* If we have succesfully downloaded and stored the file. We are then
       going to unzip this file here.
     */
    protected void onPostExecute(String strResponse)
    {
        if (strResponse.equals("SUCCESS"))
        {
            try
            {
                extractZipFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}