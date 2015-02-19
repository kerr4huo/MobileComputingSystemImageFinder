package in.ke.mcs.myimagematcher;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements CvCameraViewListener2 {
	private static final String TAG = "OCVSample::Activity";
	private CameraBridgeViewBase mOpenCvCameraView;
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public MainActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	ImageView matchDrawArea;
	Button loadFeatureBtn;
	TextView matchImagePath;
	TextView matchImagePath1;
	TextView matchImagePath2;
	TextView matchImagePath3;
	TextView matchImagePath4;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.main_layout);
		matchDrawArea = (ImageView) findViewById(R.id.refImageView);
		loadFeatureBtn = (Button) findViewById(R.id.button6);
		matchImagePath = (TextView)findViewById(R.id.textView1);
		matchImagePath1 = (TextView)findViewById(R.id.textView2);
		matchImagePath2 = (TextView)findViewById(R.id.textView3);
		matchImagePath3 = (TextView)findViewById(R.id.textView4);
		matchImagePath4 = (TextView)findViewById(R.id.textView5);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_native_surface_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});

	}

	boolean resize = false;

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {

	}

	public void onCameraViewStopped() {
	}

	boolean showOriginal = true;

	public void cameraclick(View w) {
		showOriginal = !showOriginal;
	}

	Mat last;
	ArrayList<Scene> scenes = new ArrayList<Scene>();
	Scene refScene;
	ProgressDialog progress;
	ProgressDialog picProgress;
	ProgressDialog featureProgress;

	/*
	public void takePic1(View w) {
		Scene scene = new Scene(last);
		scenes.add(scene);
		addBtn.setText("Add (" + scenes.size() + ")");
	}*/

	Mat queryDescriptors;
	
	public void takePic2(View w) {
		
		Mat im = last.clone();
		// Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2RGB);
		Bitmap bmp = Bitmap.createBitmap(im.cols(), im.rows(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(im, bmp);
		Bitmap bmpSmall = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth()*0.2), (int) (bmp.getHeight()*0.2), false);
		matchDrawArea.setImageBitmap(bmpSmall);
		
		refScene = new Scene(last);
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final File jpg_file = new File("/sdcard/DCIM/IMG2Search/pic2search.jpg");

		//Imgproc.cvtColor(im, pic2search_Gray, Imgproc.COLOR_BGR2GRAY);
		
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        try {
        	jpg_file.createNewFile();
                FileOutputStream fo = new FileOutputStream(jpg_file);
                fo.write(bytes.toByteArray());
                fo.close();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        if (queryDescriptors != null){
        	queryDescriptors.release();
        }
        queryDescriptors = new Mat();
        
        String filePathName = jpg_file.getAbsolutePath();
        int log = NonfreeJNILib.findFeatures4Search(filePathName, queryDescriptors.getNativeObjAddr());
	}
	
	public void loadPicsCheck(View w) {
		
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Load database pictures");
		alertDialog.setMessage("Sure??????????");
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						loadPics();
					}
				});
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				});
		alertDialog.show();
		
		//
		//NonfreeJNILib.runDemo(1);
	}
	
	public void loadPics(){
		new LoadPicsFindFeatures(this).execute();
	}
	
	class LoadPicsFindFeatures extends AsyncTask<Void, Integer, Integer> {
		Context context;

		public LoadPicsFindFeatures(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			picProgress = new ProgressDialog(context);
			picProgress.setCancelable(false);
			picProgress.setMessage("Starting to Load Images");
			picProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			picProgress.setProgress(1);
			picProgress.setMax(scenes.size());
			picProgress.show();
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			picProgress.setProgress(values[0]);
			
			super.onProgressUpdate(values);
		}

		int picIndex = 0;
		@Override
		protected Integer doInBackground(Void... params) {

			long s = System.currentTimeMillis();
			String directoryName;
			directoryName = "/sdcard/DCIM/PublicImages";
			File directory = new File(directoryName);
			String[] extensions = {"jpg"};
			
			File[] fileArray;
			
			Collection<File> files = FileUtils.listFiles(directory, extensions, true);
			fileArray = files.toArray(new File[files.size()]);

			Mat imageIn;
			int log = 0;
			String filePathName;
			int count = fileArray.length;
			for (int i = 0; i < count; i++){
				publishProgress((int) ((i / (float) count) * 100));
				
				filePathName = fileArray[i].toString();
				
				log = NonfreeJNILib.findFeatures(filePathName);
				picIndex = i;
				Log.i("LoadPics", Integer.toString(picIndex));
			}
			long e = System.currentTimeMillis();
			
			return picIndex;
		}

		@Override
		protected void onPostExecute(Integer result) {
			picProgress.dismiss();
		}
	}
	
	//ArrayList<Mat> descriptorsArray = new ArrayList<Mat>();
	ArrayList<String> descriptorNameArray = new ArrayList<String>();
	String[] fileNameArray = new String[0];
	public void loadFeatures(View w){

		new LoadFeatures(this).execute();
	}
	
	class LoadFeatures extends AsyncTask<Void, Integer, Integer> {
		Context context;

		public LoadFeatures(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			featureProgress = new ProgressDialog(context);
			featureProgress.setCancelable(false);
			featureProgress.setMessage("Starting to Load Features");
			featureProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			featureProgress.setProgress(1);
			featureProgress.setMax(scenes.size());
			featureProgress.show();
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			featureProgress.setProgress(values[0]);
			
			super.onProgressUpdate(values);
		}

		int featureIndex = 0;
		@Override
		protected Integer doInBackground(Void... params) {
			
			long s = System.currentTimeMillis();
			String directoryName = "/sdcard/DCIM/PublicImages";
			File directory = new File(directoryName);
			String[] extensions = {"xml"};
			
			File[] fileArray;
			
			Collection<File> files = FileUtils.listFiles(directory, extensions, true);
			fileArray = files.toArray(new File[files.size()]);

			fileNameArray = new String[fileArray.length];
			for (int i = 0; i< fileArray.length; i++){
				fileNameArray[i] = fileArray[i].toString();
			}
			Log.i("loadFeatures", String.valueOf(fileArray.length));
			// load features for matcher
			
			float log = NonfreeJNILib.loadFeaturesTrainMatcher(fileNameArray);
			Log.i("loadFeatures", String.valueOf(log));
			//Log.i("loadFeatures", fileNameArray[(int) log]);
			
			/*
			int log = 0;
			String filePathName;
			Mat descriptor = new Mat();
			int count = fileArray.length;
			for (int i = 0; i < count; i++){
				publishProgress((int) ((i / (float) count) * 100));
				filePathName = fileArray[i].toString();
				log = NonfreeJNILib.loadFeatures(filePathName, descriptor.getNativeObjAddr());
				descriptorsArray.add(descriptor);
				
				if (filePathName.indexOf("image") != -1){
					descriptorNameArray.add(filePathName.substring(filePathName.indexOf("image", 0), 
							filePathName.indexOf(".")));
				}
				
				featureIndex = i;
			
				Log.i("LoadFeatures", Integer.toString(featureIndex));
			}*/
			long e = System.currentTimeMillis();
			
			return featureIndex;
		}

		@Override
		protected void onPostExecute(Integer result) {
			featureProgress.dismiss();
			loadFeatureBtn.setText("Load Database Features(" + fileNameArray.length+ ")");
		}
	}
	
	public void findMatch(View w){
		
	}
	
	public void compareClick(View w) {
		if (fileNameArray.length == 0) {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Load database features first");
			alertDialog
					.setMessage("You should load database features first.");
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					});
			alertDialog.show();
		} else if (queryDescriptors == null) {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("No reference image.");
			alertDialog.setMessage("You should take a reference image to compare with the database");
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

						}
					});

			alertDialog.show();

		} else {
			new FindMatch(this).execute();
		}
	}


	class FindMatch extends AsyncTask<Void, Integer, String[]> {
		Context context;
		Bitmap bmp;

		public FindMatch(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			progress = new ProgressDialog(context);
			progress.setCancelable(false);
			progress.setMessage("Starting to Find Matches");
			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progress.setProgress(1);
			progress.setMax(scenes.size());
			progress.show();
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progress.setProgress(values[0]);
			super.onProgressUpdate(values);
		}

		@Override
		protected String[] doInBackground(Void... params) {
			long s = System.currentTimeMillis();
			
			String[] matchedPicNames = new String[4];

			if (queryDescriptors.empty()){
				Log.i("FindMatch", "EMPTY QUERY Descriptor");
			}
			//float log  = NonfreeJNILib.findMatches(queryDescriptors.getNativeObjAddr(), descriptorsPtrArray);
			int[] bestMatchInx = NonfreeJNILib.loadMatcherFindMatch(queryDescriptors.getNativeObjAddr(), 10);
			Log.i("FindMatch", String.valueOf(bestMatchInx[0]));
			Log.i("FindMatch", fileNameArray[bestMatchInx[0]]);
			
			
			Log.i("FindMatch", String.valueOf(bestMatchInx[0]));
			Log.i("FindMatch", fileNameArray[bestMatchInx[0]]);
			Log.i("FindMatch", String.valueOf(bestMatchInx[1]));
			Log.i("FindMatch", fileNameArray[bestMatchInx[1]]);
			Log.i("FindMatch", String.valueOf(bestMatchInx[2]));
			Log.i("FindMatch", fileNameArray[bestMatchInx[2]]);
			Log.i("FindMatch", String.valueOf(bestMatchInx[3]));
			Log.i("FindMatch", fileNameArray[bestMatchInx[3]]);
			
			for (int i = 0; i < 4; i++){
				matchedPicNames[i] = fileNameArray[bestMatchInx[i]];
			}
			
			long e = System.currentTimeMillis();

			return matchedPicNames;
		}

		@Override
		protected void onPostExecute(String[] matchedPicNames) {
			// info.setText(result);
			progress.dismiss();
			matchImagePath.setText(matchedPicNames[0]);
			String directoryName = "/sdcard/DCIM/PublicImages";
			String picName;
			picName = matchedPicNames[0].substring(directoryName.length());
			matchImagePath1.setText("  1:  " + picName);
			picName = matchedPicNames[1].substring(directoryName.length());
			matchImagePath2.setText("  2:  " + picName);
			picName = matchedPicNames[2].substring(directoryName.length());
			matchImagePath3.setText("  3:  " + picName);
			picName = matchedPicNames[3].substring(directoryName.length());
			matchImagePath4.setText("  4:  " + picName);
		}
	}
	
	public void removeAll(View w) {
		scenes.clear();
		if (queryDescriptors != null){
			queryDescriptors.release();
        }
		//descriptorsArray.clear();
		//loadFeatureBtn.setText("Load Database Features(" + descriptorsArray.size()+ ")");
	}
/*
	class BetterComparePics extends AsyncTask<Void, Integer, SceneDetectData> {
		Context context;

		public BetterComparePics(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			progress = new ProgressDialog(context);
			progress.setCancelable(false);
			progress.setMessage("Starting to Find Matches");
			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progress.setProgress(1);
			progress.setMax(scenes.size());
			progress.show();
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progress.setProgress(values[0]);
			super.onProgressUpdate(values);
		}

		@Override
		protected SceneDetectData doInBackground(Void... params) {
			long s = System.currentTimeMillis();
			Scene max = null;
			SceneDetectData maxData = null;
			int maxDist = -1;
			int idx = -1;
			for (int i = 0; i < scenes.size(); i++) {
				Scene scn = scenes.get(i);
				SceneDetectData data = refScene.compare(scn, ransacEnabled,
						imageOnly);
				int currDist;
				if (ransacEnabled) {
					currDist = data.homo_matches;
				} else {
					currDist = data.dist_matches;
				}

				if (currDist > maxDist) {
					max = scn;
					maxData = data;
					maxDist = currDist;
					idx = i;
				}
				this.publishProgress(i + 1);
			}

			bmp = maxData.bmp;
			long e = System.currentTimeMillis();
			maxData.elapsed = e - s;
			maxData.idx = idx;

			return maxData;
		}

		@Override
		protected void onPostExecute(SceneDetectData maxData) {
			// info.setText(result);
			progress.dismiss();

			final Dialog settingsDialog = new Dialog(context);
			settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			settingsDialog.setContentView(getLayoutInflater().inflate(
					R.layout.image_layout, null));
			ImageView im = (ImageView) settingsDialog
					.findViewById(R.id.imagePopup);
			Button dismiss = (Button) settingsDialog
					.findViewById(R.id.dismissBtn);
			TextView info = (TextView) settingsDialog
					.findViewById(R.id.infoText);

			im.setImageBitmap(bmp);
			dismiss.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					settingsDialog.dismiss();
				}
			});

			info.setText(maxData.toString());

			settingsDialog.show();

			super.onPostExecute(maxData);
		}
	}
*/

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		last = inputFrame.rgba();
		return last;
	}
}
