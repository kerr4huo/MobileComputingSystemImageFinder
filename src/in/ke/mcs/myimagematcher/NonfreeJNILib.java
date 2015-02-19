package in.ke.mcs.myimagematcher;

public class NonfreeJNILib {
	 static 
	    {
		     try
		     { 
			     // Load necessary libraries.
			     System.loadLibrary("opencv_java");
			     System.loadLibrary("nonfree");
			     System.loadLibrary("nonfree_jni");
		     }
		     catch( UnsatisfiedLinkError e )
		     {
		           System.err.println("Native code library failed to load.\n" + e); 
		     }
	    }
	 
	    public static native void runDemo(int i);
	    public static native int findFeatures(String filePathName);
	    public static native int loadFeatures(String filePathName, long outMatPtr);
	    public static native float loadFeaturesTrainMatcher(String[] filePathNameArray);
	    
	    public static native int findFeatures4Search(String filePathName, long outMatPtr);
	    public static native float findMatches(long inMatPtr, long[] outMatPtr);
	    public static native int[] loadMatcherFindMatch(long inMatPtr, int size);
}
