package sdgnys.outpostlite.sdgnys.outpostlite.access;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static sdgnys.outpostlite.Logger.log;

/**
 * Created by jforce on 8/8/2017.
 */
public class StorageAccess {
	
	public final static String EXPORT_DIRECTORY_NAME = "export/";
	
	public final File exportDirectory;
 	private AppCompatActivity context;
	
	public StorageAccess(AppCompatActivity context) {
		this.context = context;
		requestStorageAccess();

		exportDirectory = new File(context.getFilesDir().getPath() + "/" + EXPORT_DIRECTORY_NAME);
		if (!exportDirectory.exists())
			exportDirectory.mkdir();
	}
	
	/** @return true if the specified file is an image. This is determined by its file ending. */
	public boolean isImage(File file) {
		String[] endingSplit = file.getName().split(Pattern.quote("."));
		if (endingSplit.length <= 1)
			return false;
		else
			return endingSplit[endingSplit.length - 1].equals("jpg");
	}
	
	/** This lists all the internally stored files on the device for this application. */
	public void listInternalFiles() {
		log("Printing internal files : ");
		for (File f : context.getFilesDir().listFiles())
			log(f.getPath());
		
		log("In export directory : ");
		for (File f : exportDirectory.listFiles())
			log(f.getPath());
	}
	
	/** Creates a new File in the Download folder with the specified name. */
	public File createExternalFile(String name) throws IOException {
		File fl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		
		File newFile = new File(fl, name);
		newFile.createNewFile();
		return newFile;
	}
	
	/** Creates a new File in the applications internal storage. */
	public File createInternalFile(String name) throws IOException {
		File fl = context.getFilesDir();
		
		File newFile = new File(fl, name);
		newFile.createNewFile();
		return newFile;
	}
	
	/** Returns a File on the internal storage space with the specified name. */
	public File getInternalFile(String name) {
		// For every file on the internal storage system,
		for (File f : context.getFilesDir().listFiles())
			// If the name matches,
			if (f.getName().equals(name))
				return f;
		throw new RuntimeException("Couldn't find file " + name);
	}
	
	/** Delete all local data from the internal storage. Use with caution.
	 * only deletes .txt and .jpg files. */
	public void deleteLocalFiles() {
		for (File f : exportDirectory.listFiles())
			f.delete();
		// For every file on the internal storage system,
		for (File f : context.getFilesDir().listFiles())
			if (getExtension(f).equals("txt") ||
					getExtension(f).equals("jpg")) {
				if (f.delete())
					continue;
				else
					throw new RuntimeException("Couldn't delete file : " + f.getName());
			}
	}
	
	/** This method searches through all images in the directory associated with this parcel and
	 * generates a unique int that hasn't been used before.
	 * @param directory The directory to look for images associated with this parcel in.
	 * @return A unique int not used by any other images associated with this parcel.
	 */
	public int getUniqueImageId(File directory, String PRINT_KEY) {
		// We will find a unique id by finding the highest ID and then incrementing it.
		int highestId = 0;
		for (File file : directory.listFiles())
			if (file.isDirectory())
				// Search that directory for its highest ID and make it ours if it's higher.
				highestId = Math.max(highestId, getUniqueImageId(file, PRINT_KEY));
				// If the file is an image and associated with this parcel,
			else if (isImage(file) && file.getName().contains(PRINT_KEY)) {
				int id = getFileID(file);
				if (id > highestId)
					highestId = id;
			}
		return highestId + 1;
	}
	
	/** Set a File's default status. Does not seek out and eliminate other defaults,
	 * just marks this file to be a default.
	 * @param file The file to change to be default or not.
	 * @param isDefault True if the File should become the default. */
	public void setFileIsDefault(File file, boolean isDefault) {
		String[] split = file.getName().split(Pattern.quote("_"));
		
		String flag = (isDefault)? "1" : "0";
		
		String newName =
				file.getPath().substring(0, file.getPath().lastIndexOf('/')) + "/" +
						split[0]+"_"+split[1]+"_"+split[2]+"_"+flag+"_"+split[4];
		
		if (file.renameTo(new File(newName)))
			;// Success!
		else
			throw new RuntimeException("Couldn't rename file.");
	}
	
	/** @return the filename that should be used for a new image for the parcel. */
	public String getNewImageFileName(String SWIS, String PRINT_KEY, String PARCEL_ID) {
		return SWIS + "_" + PRINT_KEY + "_" + PARCEL_ID + "_0_" + getUniqueImageId(context.getFilesDir(), PRINT_KEY) + ".jpg";
	}
	
	/** Read the specified file from disk as a Bitmap. */
	public Bitmap getImageBitmap(File file) {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),bmOptions);
		return bitmap;
	}
	
	/** Compress a file on disk using the specified compression parameters. */
	public void compressJPG(File file, int compressionFactor, int sampleSize) {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = sampleSize;
		Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),bmOptions);
		file.delete();
		saveBitmap(bitmap, file, compressionFactor);
	}
	
	/** This method was shamelessly stolen from
	 * https://stackoverflow.com/questions/649154/save-bitmap-to-location */
	public void saveBitmap(Bitmap bmp, File file, int compressionFactor) {
		FileOutputStream out = null;
		try {
			if (!file.exists())
				file.createNewFile();
			out = new FileOutputStream(file.getPath());
			bmp.compress(Bitmap.CompressFormat.JPEG, compressionFactor, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/** Get the SWIS from the specified File's name. */
	public String getFileSWIS(File f) {
		return f.getName().split(Pattern.quote("_"))[0];
	}
	
	/** Get the PRINT_KEY from the specified File's name. */
	public String getFilePrintKey(File f) {
		return f.getName().split(Pattern.quote("_"))[1];
	}
	
	/** Get the PARCEL_ID from the specified file's name */
	public String getFileParcelID(File f) {
		return f.getName().split(Pattern.quote("_"))[2];
	}
	
	/** @return true if the specified File is the default image according to it's name */
	public boolean getFileIsDefault(File f) {
		return f.getName().split(Pattern.quote("_"))[3].equals("1");
	}
	
	/** @return the unique integer that identifies this image. */
	public int getFileID(File f) {
		return Integer.parseInt(f.getName().split(Pattern.quote("_"))[4].split(Pattern.quote("."))[0]);
	}
	
	/** Unzips the newest incoming package into internal storage. */
	public void loadPackage(Callback<Float> progressCallback) {
		unzip(getIncomingPackage(), context.getFilesDir(), progressCallback);
	}
	
	/** Gets the incoming package that has been placed in the Download directory by
	* the desktop application. */
	private File getIncomingPackage() {
		// Get the Download directory.
		File fl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		// For every file in that directory,
		File target = null;
		for (File f : fl.listFiles())
			// Look for one with the right name.
			if (f.getName().equals("PCToTablet.zip")) {
				target = f;
				break;
			}
		return target;
	}
	
	/** This method is used to request permission to access the filesystem.
	 * As of API level 23, you need to request the permission, you cant just put it into the manifest.
	 * @return True if the app has access to read and write to the filesystem.
	 */
	private boolean requestStorageAccess() {
		if (Build.VERSION.SDK_INT >= 23) {
			if (context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_GRANTED) {
				return true;
			} else {
				ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
				return false;
			}
		}
		else { //permission is automatically granted on sdk<23 upon installation
			Log.v("OutpostLite","Permission is granted");
			return true;
		}
	}
	
	private void unzip(File zip, File outputDirectory, Callback<Float> progressCallback)
	{
		InputStream is;
		ZipInputStream zis;
		try
		{
			is = new FileInputStream(zip);
			zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry ze;
			long bytesWritten = 0L;
			
			while((ze = zis.getNextEntry()) != null)
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int count;
				
				String filename = ze.getName();
				FileOutputStream fout = new FileOutputStream(outputDirectory.getPath() + "/" + filename);
				
				// reading and writing
				while((count = zis.read(buffer)) != -1)
				{
					baos.write(buffer, 0, count);
					byte[] bytes = baos.toByteArray();
					bytesWritten += bytes.length;
					fout.write(bytes);
					baos.reset();
				}
				progressCallback.callback(100 * ((float) bytesWritten) / ((float) zip.getTotalSpace()));
				
				fout.close();
				zis.closeEntry();
			}
			
			zis.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/** @return the file extension of the given file. Ex : "poop.txt" -> txt
	 * https://stackoverflow.com/questions/3571223/how-do-i-get-the-file-extension-of-a-file-in-java */
	private String getExtension(File file) {
		String extension = "";
		
		int i = file.getName().lastIndexOf('.');
		if (i > 0) {
			extension = file.getName().substring(i+1);
		}
		
		return extension;
	}
}
