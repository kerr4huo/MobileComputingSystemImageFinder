#include <jni.h>
#include <utility>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/flann/flann.hpp>
#include <opencv2/flann/flann_base.hpp>
#include <opencv2/flann/miniflann.hpp>
#include <opencv2/nonfree/features2d.hpp>
#include <opencv2/nonfree/nonfree.hpp>

#include <android/log.h>

#include <vector>

using namespace std;
using namespace cv;

void run_Demo();
//void findFeatures(vector<string> filePathArray);

bool sortByDistance(const DMatch &lhs, const DMatch &rhs) {return lhs.distance < rhs.distance ;}
bool sortByValue(const int i, const int j) {return i > j;}
bool sortByImgIdx(const DMatch &lhs, const DMatch &rhs) {return lhs.imgIdx < rhs.imgIdx ;}
bool ifEquals(const DMatch &lhs, const int i) {return lhs.imgIdx == i;}
// JNI interface functions, be careful about the naming.
extern "C"
{
    JNIEXPORT void JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_runDemo(JNIEnv * env, jobject obj, jint i);
    JNIEXPORT jint JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_findFeatures(JNIEnv * env, jobject obj, jstring filePathName);
    JNIEXPORT jint JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_loadFeatures(JNIEnv * env, jobject obj, jstring filePathName, jlong outMat);
    JNIEXPORT jfloat JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_loadFeaturesTrainMatcher(JNIEnv * env, jobject obj, jobjectArray filePathNameArray);
    JNIEXPORT jint JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_findFeatures4Search(JNIEnv * env, jobject obj, jstring filePathName, jlong outMat);
    JNIEXPORT jfloat JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_findMatches(JNIEnv * env, jobject obj, jlong queryDescriptorsPtr, jlongArray descriptorsPtrArray);
    JNIEXPORT jintArray JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_loadMatcherFindMatch(JNIEnv * env, jobject obj, jlong queryDescriptorsPtr, jint size);
    //JNIEXPORT jfloat JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_findMatches(JNIEnv * env, jobject obj, jlong queryDescriptorsPtr, jlongArray descriptorsPtrArray);

}

JNIEXPORT void JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_runDemo(JNIEnv * env, jobject obj, jint i)
{
	run_Demo();
}

JNIEXPORT jint JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_findFeatures(JNIEnv * env, jobject obj, jstring filePathName)
{
	// path name length
		int pathLength;
		char* descriptorFileName;
		const char* myarray;

		// detect and store the descriptor to files
		Mat image;
		Mat grayImage;
		vector<KeyPoint> keypoints;
		Mat descriptors;
		int minHessian = 400;
		SurfFeatureDetector detector(minHessian);

		SurfDescriptorExtractor extractor;

		myarray = env->GetStringUTFChars(filePathName, 0);
		image = imread(myarray, CV_LOAD_IMAGE_COLOR);

		/*
		// reduce resolution for hobbit and trash;
		if (strstr(myarray, "Hobbit") != NULL) {
			resize(image, image, Size(), 2.5, 2.5);
			cvtColor(image, image, CV_RGB2GRAY );
		}else if (strstr(myarray, "Trash") != NULL){
			resize(image, image, Size(), 2.5, 2.5);
			cvtColor(image, image, CV_RGB2GRAY );
		}else {
			cvtColor(image, image, CV_RGB2GRAY );
		}*/

		cvtColor(image, grayImage, CV_RGB2GRAY );

		detector.detect(grayImage, keypoints);
		extractor.compute(grayImage, keypoints, descriptors);
		//detector.compute(image,keypoints, descriptors);
		//strcpy(descriptorFileName, myarray);

		strncpy(strstr(myarray, "jpg"), "xml", 3);
		FileStorage fs(myarray, FileStorage::WRITE);
	    fs << "descriptors" << descriptors;
		fs.release();

		env->ReleaseStringUTFChars(filePathName, myarray);
		return 0;
}

JNIEXPORT jint JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_loadFeatures(JNIEnv * env, jobject obj, jstring filePathName, jlong outMatPtr)
{
	int pathLength;
	char* descriptorFileName;
	const char* myarray;
	Mat *pDescriptors = (Mat*) outMatPtr;
	myarray = env->GetStringUTFChars(filePathName, 0);

	FileStorage fs(myarray, FileStorage::READ);
	fs ["descriptors"] >> *pDescriptors;
	fs.release();

	env->ReleaseStringUTFChars(filePathName, myarray);

	return 0;
}

FlannBasedMatcher matcher;

JNIEXPORT jfloat JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_loadFeaturesTrainMatcher(JNIEnv * env, jobject obj, jobjectArray filePathNameArray)
{
	std::vector<Mat> trainDescriptors;
	int fileCount = env->GetArrayLength(filePathNameArray);

	for  (int i = 0; i < fileCount; i++){
		jstring string = (jstring) env->GetObjectArrayElement(filePathNameArray, i);

		if (string == NULL){
			return  -1;
		}
		const char* myarray = env->GetStringUTFChars(string, NULL);
		Mat *pTrainDescriptor = new Mat();
		FileStorage fs(myarray, FileStorage::READ);
		fs ["descriptors"] >> *pTrainDescriptor;
		fs.release();
		trainDescriptors.push_back(*pTrainDescriptor);
		pTrainDescriptor->release();
		env->ReleaseStringUTFChars(string, myarray);
	}

	__android_log_write(ANDROID_LOG_ERROR, "Train Matcher", "Start");
	std::vector<DMatch> matches;
	matcher.add(trainDescriptors);
	trainDescriptors.clear();
	matcher.train();

	__android_log_write(ANDROID_LOG_ERROR, "Train Matcher", "End");

	return  -1;
}

JNIEXPORT jint JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_findFeatures4Search(JNIEnv * env, jobject obj, jstring filePathName, jlong outMatPtr){

	char* descriptorFileName;
	const char* myarray;
	Mat *pDescriptors = (Mat*) outMatPtr;

	// detect and store the descriptor to files
	Mat image;
	Mat grayImage;
	vector<KeyPoint> keypoints;
	int minHessian = 400;
	SurfFeatureDetector detector(minHessian);

	SurfDescriptorExtractor extractor;

	myarray = env->GetStringUTFChars(filePathName, 0);
	image = imread(myarray, CV_LOAD_IMAGE_COLOR);
	cvtColor(image, grayImage, CV_RGB2GRAY );

	detector.detect(grayImage, keypoints);
	extractor.compute(grayImage, keypoints, *pDescriptors);

	env->ReleaseStringUTFChars(filePathName, myarray);
	return 0;
}

JNIEXPORT jfloat JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_findMatches(JNIEnv * env, jobject obj, jlong queryDescriptorsPtr, jlongArray descriptorsPtrArray){

	Mat *pQueryDescriptors = (Mat*) queryDescriptorsPtr;

	std::vector<Mat> trainDescriptors;
	jlong *pTranDescriptorsPtrArray = env->GetLongArrayElements(descriptorsPtrArray, NULL);

	//int count = env->GetArrayLength(descriptorsPtrArray);
	int count = 5;
	for (int i = 0; i < count; i++){
		Mat *pTrainDescriptor = (Mat*) pTranDescriptorsPtrArray[i];
		trainDescriptors.push_back(*pTrainDescriptor);
		//pTrainDescriptor->release();
	}

	env->ReleaseLongArrayElements(descriptorsPtrArray, pTranDescriptorsPtrArray, 0);
	FlannBasedMatcher matcher;
	std::vector<DMatch> matches;
	matcher.add(trainDescriptors);
	trainDescriptors.clear();

	//matcher.train();
	if (pQueryDescriptors->empty()){
		//return -1;
	}
	matcher.match(*pQueryDescriptors, matches);

	std::vector<DMatch> good_matches;
	for (int i = 0; i < matches.size(); i++){
		if( matches[i].distance <= 0.05 )
		{

		}
	}

	return matches[500].imgIdx;

	return -1;
}

JNIEXPORT jintArray JNICALL Java_in_ke_mcs_myimagematcher_NonfreeJNILib_loadMatcherFindMatch(JNIEnv * env, jobject obj, jlong queryDescriptorsPtr, jint size){

	jintArray result;
	result = env->NewIntArray(size);
	if (result == NULL) {
	     return NULL; /* out of memory error thrown */
	 }

	__android_log_write(ANDROID_LOG_ERROR, "Match", "Start");

	std::vector<DMatch> matches;
	Mat *pQueryDescriptors = (Mat*) queryDescriptorsPtr;
	matcher.match(*pQueryDescriptors, matches);

	double max_dist = 0; double min_dist = 100;
	for( int i = 0; i < matches.size(); i++ ){
		double dist = matches[i].distance;
	    if( dist < min_dist ) min_dist = dist;
	    if( dist > max_dist ) max_dist = dist;
	}

	std::vector< DMatch > good_matches;
	//max(2*min_dist, 0.02)

	for( int i = 0; i < matches.size(); i++ )
	{ if( matches[i].distance <= .1)
		good_matches.push_back( matches[i]);
	}

	vector< pair<int, int> > imageCount;
	int mycount = 0;
	for (int i = 0; i < 101; i++){
		mycount = 0;
		for (int j = 0; j < good_matches.size(); j++){
			if (good_matches[j].imgIdx == i)
				mycount++;
		}
		imageCount.push_back(make_pair<int, int>(mycount,i));
	}
	sort(imageCount.begin(), imageCount.end());
	__android_log_write(ANDROID_LOG_ERROR, "Match", "End");

	jint bestMatches[size];
	for (int i = 0; i < size; i++){
		bestMatches[i] = imageCount.back().second;
		imageCount.pop_back();
	}

	env->SetIntArrayRegion(result, 0, size, bestMatches);
	return result;
}

void run_Demo()
{
    const char * imgInFile = "/sdcard/DCIM/nonfree/img1.jpg";
	const char * imgOutFile = "/sdcard/DCIM/nonfree/img1_result.jpg";

	Mat image;
	Mat imageOut;
	image = imread(imgInFile, CV_LOAD_IMAGE_COLOR);
	imageOut = image;

	vector<KeyPoint> keypoints;
	Mat descriptors;

	SiftFeatureDetector detector;
	detector.detect(image, keypoints);
	detector.compute(image,keypoints, descriptors);

    for( unsigned int i = 0; i < keypoints.size(); i++ )
    {
        const KeyPoint& kp = keypoints[i];
        circle(imageOut, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
    }

    FileStorage fs("/sdcard/DCIM/nonfree/img1.xml", FileStorage::WRITE);
    fs << "descriptors" << descriptors;
    fs.release();

    imwrite( imgOutFile, imageOut);
       /* Some other processing, please check the download package for details. */

}







