package com.example.top10downloader

import android.util.Log
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL


private const val TAG = "DownloadData"

class DownloadData(private val callBack: DownloaderCallBack)  {

    interface DownloaderCallBack {
        fun onDataAvailable(data: List<FeedEntry>)
    }
    fun createObservable(urlPath: String) {
        Log.d(TAG, "createObservable: starts with $urlPath")
//        val rssFeed = downloadXML(urlPath)
        val observer = object : Observer<String?> {


            override fun onError(error: Throwable) {
                Log.e(TAG, "doInBackground: Error downloading with $error")
            }

            override fun onComplete() {
                Log.d(TAG, "onComplete called ")

            }

            override fun onSubscribe(d: Disposable?) {
                Log.d(TAG, "onSubscribe called with $d")
            }

            override fun onNext(result: String?) {
                val parseApplications = ParseApplications()
                if(result?.isNotEmpty() == true) {
                    parseApplications.parse(result)
                }
                callBack.onDataAvailable(parseApplications.applications)
            }
        }
        createObservables(urlPath).subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(observer)
    }


//     fun onPostExecute(result: String) {
//
//        val parseApplications = ParseApplications()
//        if(result.isNotEmpty()) {
//            parseApplications.parse(result)
//        }
//
//        callBack.onDataAvailable(parseApplications.applications)
//    }
//
//     fun doInBackground(vararg url: String): String {
//        Log.d(TAG, "doInBackground: starts with ${url[0]}")
//        val rssFeed = downloadXML(url[0])
//        if (rssFeed.isEmpty()) {
//            Log.e(TAG, "doInBackground: Error downloading")
//        }
//        return rssFeed
//    }
private fun createObservables(urlPath: String): Observable<String> = Observable.fromCallable { downloadXML(urlPath)}

    private fun downloadXML(urlPath: String): String {
        try {
            return URL(urlPath).readText()        }
        catch(e: MalformedURLException) {
            Log.d(TAG, "downloadXML: Invalid URL " + e.message)
        } catch(e: IOException) {
            Log.d(TAG, "downloadXML: IO Exception reading data " + e.message)
        } catch(e: SecurityException) {
            Log.d(TAG, "downloadXML: Security exception. Needs permission? " + e.message)
//            e.printStackTrace()
        }
        return ""    // return an empty string if there was an exception
    }
}