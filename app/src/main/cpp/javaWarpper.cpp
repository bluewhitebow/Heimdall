#include <jni.h>
#include <string>
#include "include/Pipeline.h"

std::string jstring2str(JNIEnv* env, jstring jstr) {
    char*   rtn   =   NULL;
    jclass   clsstring   =   env->FindClass("java/lang/String");
    jstring   strencode   =   env->NewStringUTF("GB2312");
    jmethodID   mid   =   env->GetMethodID(clsstring,   "getBytes",   "(Ljava/lang/String;)[B");
    jbyteArray   barr=   (jbyteArray)env->CallObjectMethod(jstr,mid,strencode);
    jsize   alen   =   env->GetArrayLength(barr);
    jbyte*   ba   =   env->GetByteArrayElements(barr,JNI_FALSE);
    if(alen > 0) {
        rtn = (char*)malloc((size_t) (alen + 1));
        memcpy(rtn, ba, (size_t) alen);
        rtn[alen]=0;
    }
    env->ReleaseByteArrayElements(barr,ba,0);
    std::string stemp(rtn);
    free(rtn);
    return   stemp;
}

extern "C" {
JNIEXPORT jlong JNICALL
Java_pr_platerecognization_PlateRecognition_InitPlateRecognizer(JNIEnv *env, jobject,
                                                          jstring detector_filename,
                                                          jstring finemapping_prototxt, jstring finemapping_caffemodel,
                                                          jstring segmentation_prototxt, jstring segmentation_caffemodel,
                                                          jstring charRecognition_proto, jstring charRecognition_caffemodel,
                                                          jstring segmentation_free_prototxt, jstring segmentation_free_caffemodel) {

    std::string detector_path = jstring2str(env, detector_filename);
    std::string finemapping_prototxt_path = jstring2str(env, finemapping_prototxt);
    std::string finemapping_caffemodel_path = jstring2str(env, finemapping_caffemodel);
    std::string segmentation_prototxt_path = jstring2str(env, segmentation_prototxt);
    std::string segmentation_caffemodel_path = jstring2str(env, segmentation_caffemodel);
    std::string charRecognition_proto_path = jstring2str(env, charRecognition_proto);
    std::string charRecognition_caffemodel_path = jstring2str(env, charRecognition_caffemodel);
    std::string segmentation_free_prototxt_path = jstring2str(env, segmentation_free_prototxt);
    std::string segmentation_free_caffemodel_path = jstring2str(env, segmentation_free_caffemodel);

    pr::PipelinePR *PR = new pr::PipelinePR(detector_path,
                                            finemapping_prototxt_path, finemapping_caffemodel_path,
                                            segmentation_prototxt_path,
                                            segmentation_caffemodel_path,
                                            charRecognition_proto_path,
                                            charRecognition_caffemodel_path,
                                            segmentation_free_prototxt_path,
                                            segmentation_free_caffemodel_path);

    return (jlong) PR;
}

JNIEXPORT jstring JNICALL
Java_pr_platerecognization_PlateRecognition_StartRecognize(JNIEnv *env, jobject,
                                                     jlong matPtr, jlong object_pr) {
    pr::PipelinePR *PR = (pr::PipelinePR *) object_pr;
    cv::Mat &mRgb = *(cv::Mat *) matPtr;
    cv::Mat rgb;
    cv::cvtColor(mRgb,rgb,cv::COLOR_RGBA2BGR);
    cv::imwrite("/storage/emulated0/demo.jpg",rgb);

    std::vector<pr::PlateInfo> list_res= PR->RunPiplineAsImage(rgb, 0);
    std::string concat_results;
    for(auto one:list_res) {
        if (one.confidence>0.7){//it is valid plate when confidence is larger than 0.7
            concat_results+=one.getPlateName()+",";
        }
    }
    concat_results = concat_results.substr(0,concat_results.size()-1);

    return env->NewStringUTF(concat_results.c_str());
}

JNIEXPORT void JNICALL
Java_pr_platerecognization_PlateRecognition_ReleasePlateRecognizer(JNIEnv *env, jobject, jlong object_re) {
    pr::PipelinePR *PR = (pr::PipelinePR *) object_re;
    delete PR;
}
}



