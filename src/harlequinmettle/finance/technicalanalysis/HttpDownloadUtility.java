package harlequinmettle.finance.technicalanalysis;

import harlequinmettle.utils.nettools.NetPuller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.FileUtils;
 
/**
 * A utility that downloads a file from a URL.
 * @author www.codejava.net
 *
 */
public class HttpDownloadUtility {
    private static final int BUFFER_SIZE = 4096;
 
    /**
     * Downloads a file from a URL
     * @param fileURL HTTP URL of the file to be downloaded
     * @param saveDir path of the directory to save the file
     * @throws IOException
     */
    public static void downloadFile(String fileURL, String saveDir,String filename)
            throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
 
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = filename;
 
            long contentLength = httpConn.getContentLengthLong();
 
  
 
 
            String saveFilePath = saveDir + File.separator + fileName;
            //if a file already exists with the better data return from method
           //content length is always -1 sooooo...
            
             File preexisting = new File(saveFilePath);
             String previousData = "";
			if(preexisting.exists() )
            	 previousData = FileUtils.readFileToString(preexisting);
			
			
			String newData = NetPuller.getHtml2(fileURL); 
			newData = newData.replace(" ", System.lineSeparator());
			newData = newData.replaceFirst(System.lineSeparator(), " ");
			if(previousData!=null){
				System.out.println(previousData.length()+"     old/new     "+newData.length());
//				System.out.println(previousData.replaceAll(System.lineSeparator(), " "));
//				System.out.println(newData.replaceAll(System.lineSeparator(), " "));
			}
			if(previousData.length()< newData.length())
				FileUtils.writeStringToFile(preexisting, newData);

//            // opens input stream from the HTTP connection
//            InputStream inputStream = httpConn.getInputStream();
//            // opens an output stream to save into file
//            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
// 
//            int bytesRead = -1;
//            byte[] buffer = new byte[BUFFER_SIZE];
//            while ((bytesRead = inputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, bytesRead);
//            }
// 
//            outputStream.close();
//            inputStream.close();
// 
//            System.out.println("File downloaded");
//        } else {
//            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }
}
